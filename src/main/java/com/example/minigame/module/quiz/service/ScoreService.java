package com.example.minigame.module.quiz.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.minigame.module.quiz.entity.Score;
import com.example.minigame.module.quiz.mapper.ScoreMapper;
import com.example.minigame.module.quiz.vo.MyStatsVO;
import com.example.minigame.module.quiz.vo.RankRowVO;
import com.example.minigame.module.user.entity.User;
import com.example.minigame.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩读写与排行榜查询。
 */
@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreMapper scoreMapper;
    private final UserService userService;

    /** 落库一局成绩。 */
    public void save(Long userId, String category, int score, int correct, int total, int durationMs) {
        Score s = new Score();
        s.setUserId(userId);
        s.setCategory(category);
        s.setScore(score);
        s.setCorrectCount(correct);
        s.setTotalCount(total);
        s.setDurationMs(durationMs);
        scoreMapper.insert(s);
    }

    /** 某人的战绩汇总。 */
    public MyStatsVO statsOf(Long userId) {
        if (userId == null) {
            return MyStatsVO.empty();
        }
        List<Score> list = scoreMapper.selectList(Wrappers.<Score>lambdaQuery()
                .eq(Score::getUserId, userId));
        if (list.isEmpty()) {
            return MyStatsVO.empty();
        }
        int best = 0;
        int correctSum = 0;
        int totalSum = 0;
        for (Score s : list) {
            best = Math.max(best, s.getScore());
            correctSum += s.getCorrectCount();
            totalSum += s.getTotalCount();
        }
        int accuracy = totalSum == 0 ? 0 : Math.round(correctSum * 100f / totalSum);

        // 总榜名次 = 比我最高分更高的人数 + 1（同分并列，粗略但可解释）
        long better = scoreMapper.selectCount(Wrappers.<Score>lambdaQuery()
                .gt(Score::getScore, best));
        return new MyStatsVO(list.size(), best, accuracy, better + 1);
    }

    /** 最近几局，个人中心列表用。 */
    public List<Score> recent(Long userId, int limit) {
        if (userId == null) {
            return List.of();
        }
        return scoreMapper.selectList(Wrappers.<Score>lambdaQuery()
                .eq(Score::getUserId, userId)
                .orderByDesc(Score::getId)
                .last("limit " + Math.max(1, Math.min(limit, 20))));
    }

    /**
     * 排行榜：按分数降序、同分按耗时升序。
     *
     * @param category 分区 code，传 null 表示总榜
     */
    public List<RankRowVO> top(String category, int limit) {
        int size = Math.max(1, Math.min(limit, 50));
        var query = Wrappers.<Score>lambdaQuery();
        if (category != null && !category.isBlank()) {
            query.eq(Score::getCategory, category);
        }
        query.orderByDesc(Score::getScore).orderByAsc(Score::getDurationMs).last("limit " + size);

        List<Score> scores = scoreMapper.selectList(query);
        if (scores.isEmpty()) {
            return List.of();
        }

        // 一次查出涉及的用户，避免 N+1
        List<Long> userIds = scores.stream().map(Score::getUserId).distinct().toList();
        Map<Long, String> nicknames = new HashMap<>();
        for (User u : userService.listByIds(userIds)) {
            nicknames.put(u.getId(), u.getNickname() == null ? u.getUsername() : u.getNickname());
        }

        List<RankRowVO> rows = new ArrayList<>(scores.size());
        int rank = 1;
        for (Score s : scores) {
            rows.add(new RankRowVO(
                    rank++,
                    nicknames.getOrDefault(s.getUserId(), "已注销用户"),
                    s.getScore(),
                    s.getCorrectCount(),
                    s.getTotalCount(),
                    s.getDurationMs() / 1000,
                    s.getCreatedAt()
            ));
        }
        return rows;
    }

    public long count() {
        return scoreMapper.selectCount(null);
    }

    /**
     * 某人完成的有效局数，可按分区过滤（任务进度用）。
     *
     * @param category null 表示任意分区
     */
    public long countRounds(Long userId, String category) {
        if (userId == null) {
            return 0;
        }
        var query = Wrappers.<Score>lambdaQuery().eq(Score::getUserId, userId);
        if (category != null && !category.isBlank()) {
            query.eq(Score::getCategory, category);
        }
        Long n = scoreMapper.selectCount(query);
        return n == null ? 0 : n;
    }
}
