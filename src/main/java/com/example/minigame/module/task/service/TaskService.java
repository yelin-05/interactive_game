package com.example.minigame.module.task.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.minigame.module.quiz.QuizCategories;
import com.example.minigame.module.quiz.service.ScoreService;
import com.example.minigame.module.task.entity.Task;
import com.example.minigame.module.task.entity.UserTask;
import com.example.minigame.module.task.mapper.TaskMapper;
import com.example.minigame.module.task.mapper.UserTaskMapper;
import com.example.minigame.module.task.vo.TaskRowVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务中心：进度按成绩表实时计算，领取后写 t_user_task。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final UserTaskMapper userTaskMapper;
    private final ScoreService scoreService;

    /** 任务列表 + 当前用户进度（未登录时进度一律 0）。 */
    public List<TaskRowVO> listFor(Long userId) {
        List<Task> tasks = taskMapper.selectList(Wrappers.<Task>lambdaQuery()
                .eq(Task::getStatus, 1)
                .orderByAsc(Task::getId));
        if (tasks.isEmpty()) {
            return List.of();
        }

        Map<Long, Boolean> claimed = new HashMap<>();
        if (userId != null) {
            for (UserTask ut : userTaskMapper.selectList(Wrappers.<UserTask>lambdaQuery()
                    .eq(UserTask::getUserId, userId))) {
                claimed.put(ut.getTaskId(), true);
            }
        }

        return tasks.stream().map(t -> {
            int target = targetOf(t);
            long done = userId == null ? 0 : scoreService.countRounds(userId, t.getCategory());
            int progress = (int) Math.min(done, target);
            boolean isClaimed = claimed.getOrDefault(t.getId(), false);
            boolean claimable = done >= target && !isClaimed;
            QuizCategories.Def def = t.getCategory() == null ? null : QuizCategories.of(t.getCategory());
            String categoryName = def == null
                    ? (t.getCategory() == null ? "任意分区" : t.getCategory())
                    : def.name();
            return new TaskRowVO(t.getId(), t.getTitle(), t.getDescription(), t.getCategory(),
                    categoryName, target, progress, t.getRewardPoints(), claimable, isClaimed);
        }).toList();
    }

    /** 目标局数：NULL 或 ≤0 一律按 1 处理，避免页面除零和 NPE。 */
    private static int targetOf(Task t) {
        Integer n = t.getTargetCount();
        return n == null || n <= 0 ? 1 : n;
    }

    /** 领取任务奖励，返回本次获得的积分。 */
    @Transactional
    public int claim(Long userId, Long taskId) {
        if (userId == null) {
            throw new IllegalArgumentException("请先登录再领取奖励");
        }
        Task task = taskMapper.selectById(taskId);
        if (task == null || task.getStatus() == null || task.getStatus() == 0) {
            throw new IllegalArgumentException("任务不存在或已下架");
        }
        Long exist = userTaskMapper.selectCount(Wrappers.<UserTask>lambdaQuery()
                .eq(UserTask::getUserId, userId)
                .eq(UserTask::getTaskId, taskId));
        if (exist != null && exist > 0) {
            throw new IllegalArgumentException("这个任务的奖励已经领过了");
        }
        int target = targetOf(task);
        long done = scoreService.countRounds(userId, task.getCategory());
        if (done < target) {
            throw new IllegalArgumentException("还没完成，还差 " + (target - done) + " 局");
        }

        UserTask ut = new UserTask();
        ut.setUserId(userId);
        ut.setTaskId(taskId);
        ut.setRewardPoints(task.getRewardPoints());
        try {
            userTaskMapper.insert(ut);
        } catch (DuplicateKeyException e) {
            // 唯一键 (user_id, task_id) 兜底：双击 / 并发提交时上面的 selectCount 可能都放过
            throw new IllegalArgumentException("这个任务的奖励已经领过了");
        }

        log.info("用户 {} 领取任务 {} 的奖励 {} 分", userId, taskId, task.getRewardPoints());
        return task.getRewardPoints();
    }

    /** 累计已领取积分。 */
    public long totalReward(Long userId) {
        if (userId == null) {
            return 0;
        }
        List<UserTask> list = userTaskMapper.selectList(Wrappers.<UserTask>lambdaQuery()
                .eq(UserTask::getUserId, userId));
        return list.stream().mapToLong(u -> u.getRewardPoints() == null ? 0 : u.getRewardPoints()).sum();
    }
}
