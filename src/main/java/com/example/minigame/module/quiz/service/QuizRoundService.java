package com.example.minigame.module.quiz.service;

import com.example.minigame.module.quiz.QuizCategories;
import com.example.minigame.module.quiz.QuizMode;
import com.example.minigame.module.quiz.dto.QuizAnswerForm;
import com.example.minigame.module.quiz.dto.QuizSubmitForm;
import com.example.minigame.module.quiz.vo.QuizAnswerVO;
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

/**
 * 一局答题的「开局 → 逐题作答 → 交卷结算」流程，判分全部在服务端。
 *
 * <p>答案从不下发到浏览器：开局只下发题干、选项和难度，正确答案存在 Session 的 Round 里；
 * 每题作答走 answer() 即时返回对错/连击/本题得分，交卷走 submit() 统一结算并写库。
 *
 * <p>Round 存在 HttpSession（每个浏览器一个 Map），30 分钟过期，交卷后立即删除（一次性）。
 *
 * <p>计分模型：基础分按难度 简单10/中等20/困难30；连续答对第 n 题额外 (n-1)*2；
 * 答错或超时每题 -5；提前完成每秒 +2 最多 +50。总分 = max(0, 基础+连击-失误) + 速度。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizRoundService {

    private static final String SESSION_KEY = "QUIZ_ROUNDS";
    private static final long TTL_MS = 30 * 60 * 1000L;

    /** 连击加成：连续答对第 n 题时额外 (n-1)*2 分 */
    private static final int COMBO_STEP = 2;
    /** 失误惩罚：答错/超时每题扣 5 分 */
    private static final int PENALTY = 5;
    /** 速度加成上限 */
    private static final int SPEED_CAP = 50;

    /** 服务端保存的一局：完整题目（含答案，仅服务端可见）+ 玩家已答记录 */
    private record Round(String id, String category, String mode,
                         List<QuizQuestionVO> questions,
                         int secondsPerQuestion, long startedAt,
                         List<Integer> given) {
    }

    private final QuizService quizService;
    private final ScoreService scoreService;

    /* ---------------- 开局 ---------------- */

    public QuizStartVO start(HttpSession session, String category, String modeCode) {
        if (!QuizCategories.exists(category)) {
            throw new IllegalArgumentException("未知分区: " + category);
        }
        QuizMode mode = QuizMode.from(modeCode);
        List<QuizQuestionVO> picked = quizService.randomQuestions(category, mode);
        if (picked.isEmpty()) {
            throw new IllegalArgumentException("这个分区在「" + mode.label() + "」难度下还没有题目，换个难度或分区试试");
        }

        String roundId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        List<Integer> given = new ArrayList<>(picked.size());
        for (int i = 0; i < picked.size(); i++) {
            given.add(-1);
        }
        Round round = new Round(roundId, category, mode.code(), picked,
                mode.secondsPerQuestion(), System.currentTimeMillis(), given);

        Map<String, Round> rounds = rounds(session);
        long now = System.currentTimeMillis();
        rounds.entrySet().removeIf(e -> now - e.getValue().startedAt() > TTL_MS);
        rounds.put(roundId, round);

        List<QuizItemVO> items = picked.stream()
                .map(q -> new QuizItemVO(q.id(), q.question(), q.options(), q.difficulty(),
                        QuizMode.baseScore(q.difficulty())))
                .toList();

        String categoryName = QuizCategories.of(category).name();
        return new QuizStartVO(roundId, category, categoryName, mode.code(), mode.label(),
                mode.secondsPerQuestion(), items);
    }

    /* ---------------- 逐题作答（即时反馈） ---------------- */

    public QuizAnswerVO answer(HttpSession session, QuizAnswerForm form) {
        if (form == null || form.getRoundId() == null || form.getIndex() == null) {
            throw new IllegalArgumentException("缺少 roundId 或题目序号");
        }
        Round round = rounds(session).get(form.getRoundId());
        if (round == null) {
            throw new IllegalArgumentException("本局已结束或已过期，请重新开始");
        }

        int index = form.getIndex();
        if (index < 0 || index >= round.questions().size()) {
            throw new IllegalArgumentException("题目序号非法");
        }
        if (round.given().get(index) >= 0) {
            throw new IllegalArgumentException("本题已作答，不能修改");
        }

        int picked = form.getAnswer() == null ? -1 : form.getAnswer();
        round.given().set(index, picked);

        QuizQuestionVO q = round.questions().get(index);
        int correctAnswer = q.answerIndex() == null ? -1 : q.answerIndex();
        boolean ok = picked >= 0 && picked == correctAnswer;

        // 往回数连续答对的题数 = 当前连击
        int streak = 0;
        for (int i = index; i >= 0; i--) {
            int g = round.given().get(i);
            int a = round.questions().get(i).answerIndex() == null ? -1 : round.questions().get(i).answerIndex();
            if (g >= 0 && g == a) {
                streak++;
            } else {
                break;
            }
        }

        int gained;
        if (ok) {
            int base = QuizMode.baseScore(q.difficulty());
            int combo = (streak - 1) * COMBO_STEP;
            gained = base + combo;
        } else {
            gained = -PENALTY;
        }

        return new QuizAnswerVO(ok, correctAnswer, picked, q.explanation(), ok ? streak : 0, gained);
    }

    /* ---------------- 交卷结算 ---------------- */

    public QuizResultVO submit(HttpSession session, QuizSubmitForm form, Long userId) {
        if (form == null || form.getRoundId() == null) {
            throw new IllegalArgumentException("缺少 roundId");
        }
        Round round = rounds(session).remove(form.getRoundId());
        if (round == null) {
            throw new IllegalArgumentException("本局已结束或已过期，请重新开始");
        }

        int total = round.questions().size();
        List<Integer> given = round.given();
        List<Integer> formAnswers = form.getAnswers() == null ? List.of() : form.getAnswers();

        int baseScore = 0;
        int comboBonus = 0;
        int penalty = 0;
        int correct = 0;
        int streak = 0;
        int maxCombo = 0;
        List<QuizResultVO.Detail> details = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            QuizQuestionVO q = round.questions().get(i);
            int picked = given.get(i) == null ? -1 : given.get(i);
            // 兜底：没走过 /answer 的旧调用，用 form.answers 对齐
            if (picked < 0 && i < formAnswers.size() && formAnswers.get(i) != null && formAnswers.get(i) >= 0) {
                picked = formAnswers.get(i);
            }
            int yourAnswer = picked < 0 ? -1 : picked;
            int correctAnswer = q.answerIndex() == null ? -1 : q.answerIndex();
            boolean ok = picked >= 0 && picked == correctAnswer;

            int gained;
            if (ok) {
                streak++;
                maxCombo = Math.max(maxCombo, streak);
                int base = QuizMode.baseScore(q.difficulty());
                int combo = (streak - 1) * COMBO_STEP;
                gained = base + combo;
                baseScore += base;
                comboBonus += combo;
                correct++;
            } else {
                streak = 0;
                gained = -PENALTY;
                penalty += PENALTY;
            }
            details.add(new QuizResultVO.Detail(q.id(), q.question(), correctAnswer, yourAnswer, ok,
                    q.explanation(), q.difficulty() == null ? 1 : q.difficulty(), gained));
        }

        // 耗时用服务端计时，不信前端上报
        int durationMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - round.startedAt());
        int elapsedSec = durationMs / 1000;
        int limitSec = total * round.secondsPerQuestion();
        int speedBonus = Math.max(0, Math.min(SPEED_CAP, (limitSec - elapsedSec) * 2));
        int raw = baseScore + comboBonus - penalty;
        int score = Math.max(0, raw) + speedBonus;

        boolean saved = false;
        if (userId != null) {
            scoreService.save(userId, round.category(), score, correct, total, durationMs);
            saved = true;
        }

        log.debug("判分完成: category={} mode={} correct={}/{} score={} combo={} penalty={} durationMs={} saved={}",
                round.category(), round.mode(), correct, total, score, maxCombo, penalty, durationMs, saved);

        return new QuizResultVO(score, baseScore, comboBonus, penalty, speedBonus, maxCombo,
                correct, total, durationMs, saved, details);
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
