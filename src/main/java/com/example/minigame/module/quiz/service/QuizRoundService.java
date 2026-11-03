package com.example.minigame.module.quiz.service;

import com.example.minigame.module.quiz.QuizCategories;
import com.example.minigame.module.quiz.dto.QuizSubmitForm;
import com.example.minigame.module.quiz.vo.QuizItemVO;
import com.example.minigame.module.quiz.vo.QuizQuestionVO;
import com.example.minigame.module.quiz.vo.QuizResultVO;
import com.example.minigame.module.quiz.vo.QuizStartVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 一局答题的「开局 → 交卷」流程，包含服务端判题。
 *
 * <p>为什么答案不放前端：
 * 之前 /api/quiz/questions 直接把 answerIndex 发给浏览器，打开 F12 就能看到答案，
 * 成绩毫无意义。现在开局只下发题干和选项，正确答案存在 Session 里的 Round 中，
 * 交卷时由服务端比对、算分、入库。
 *
 * <p>Round 存在 HttpSession（每个浏览器一个 Map），30 分钟过期，交卷后立即删除（一次性）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizRoundService {

    /** Session 中的 key */
    private static final String SESSION_KEY = "QUIZ_ROUNDS";
    /** 一局题数 */
    public static final int PER_ROUND = 5;
    /** 每题限时（秒） */
    public static final int SECONDS_PER_QUESTION = 15;
    /** 一局有效期 */
    private static final long TTL_MS = 30 * 60 * 1000L;

    /** 服务端保存的一局：题目 id + 正确答案 */
    private record Round(String id, String category, List<Long> questionIds,
                         List<Integer> answers, long startedAt) {
    }

    private final QuizService quizService;
    private final ScoreService scoreService;

    /* ---------------- 开局 ---------------- */

    public QuizStartVO start(HttpSession session, String category) {
        if (!QuizCategories.exists(category)) {
            throw new IllegalArgumentException("未知分区: " + category);
        }
        List<QuizQuestionVO> picked = quizService.randomQuestions(category, PER_ROUND);
        if (picked.isEmpty()) {
            throw new IllegalArgumentException("这个分区还没有题目");
        }

        String roundId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Round round = new Round(
                roundId,
                category,
                picked.stream().map(QuizQuestionVO::id).toList(),
                picked.stream().map(QuizQuestionVO::answerIndex).toList(),
                System.currentTimeMillis()
        );

        Map<String, Round> rounds = rounds(session);
        long now = System.currentTimeMillis();
        rounds.entrySet().removeIf(e -> now - e.getValue().startedAt() > TTL_MS);
        rounds.put(roundId, round);

        List<QuizItemVO> items = picked.stream()
                .map(q -> new QuizItemVO(q.id(), q.question(), q.options(), q.difficulty()))
                .toList();

        String categoryName = QuizCategories.of(category).name();
        return new QuizStartVO(roundId, category, categoryName, SECONDS_PER_QUESTION, items);
    }

    /* ---------------- 交卷判分 ---------------- */

    public QuizResultVO submit(HttpSession session, QuizSubmitForm form, Long userId) {
        if (form == null || form.getRoundId() == null) {
            throw new IllegalArgumentException("缺少 roundId");
        }
        Map<String, Round> rounds = rounds(session);
        Round round = rounds.remove(form.getRoundId());
        if (round == null) {
            throw new IllegalArgumentException("本局已结束或已过期，请重新开始");
        }

        // 题目可能被下架/删除。按本局保存的题目顺序逐题对齐，
        // 否则 byIds 少返回一题就会让后面所有 answers 整体错位。
        Map<Long, QuizQuestionVO> byId = quizService.byIds(round.questionIds()).stream()
                .collect(Collectors.toMap(QuizQuestionVO::id, q -> q));
        if (byId.size() != round.questionIds().size()) {
            throw new IllegalArgumentException("题目已失效，请重新开始");
        }

        List<Integer> answers = form.getAnswers() == null ? List.of() : form.getAnswers();
        int total = round.questionIds().size();
        int correct = 0;
        List<QuizResultVO.Detail> details = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            QuizQuestionVO q = byId.get(round.questionIds().get(i));
            Integer picked = i < answers.size() ? answers.get(i) : null;
            int yourAnswer = picked == null ? -1 : picked;
            // 必须比数值：Integer 用 == 比的是引用，只有 -128..127 缓存内才碰巧相等
            boolean ok = picked != null && q.answerIndex() != null
                    && picked.intValue() == q.answerIndex();
            if (ok) {
                correct++;
            }
            details.add(new QuizResultVO.Detail(
                    q.id(), q.question(),
                    q.answerIndex() == null ? -1 : q.answerIndex(),
                    yourAnswer, ok, q.explanation()));
        }

        // 耗时用服务端计时，不信前端上报
        int durationMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - round.startedAt());
        int elapsedSec = durationMs / 1000;
        int limitSec = total * SECONDS_PER_QUESTION;

        int baseScore = correct * 20;
        // 提前完成的秒数 ×2，最多 +50，所以一局满分 100 + 50
        int speedBonus = Math.max(0, Math.min(50, (limitSec - elapsedSec) * 2));
        int score = baseScore + speedBonus;

        boolean saved = false;
        if (userId != null) {
            scoreService.save(userId, round.category(), score, correct, total, durationMs);
            saved = true;
        }

        log.debug("判分完成: category={} correct={}/{} score={} durationMs={} saved={}",
                round.category(), correct, total, score, durationMs, saved);

        return new QuizResultVO(score, baseScore, speedBonus, correct, total, durationMs, saved, details);
    }

    /* ---------------- 会话内存 ---------------- */

    @SuppressWarnings("unchecked")
    private Map<String, Round> rounds(HttpSession session) {
        Object existing = session.getAttribute(SESSION_KEY);
        if (existing instanceof Map<?, ?>) {
            return (Map<String, Round>) existing;
        }
        Map<String, Round> created = new HashMap<>();
        session.setAttribute(SESSION_KEY, created);
        return created;
    }
}
