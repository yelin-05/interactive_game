package com.example.moshi.service;

import com.example.moshi.dao.ScoreDao;
import com.example.moshi.dao.TaskClaimDao;
import com.example.moshi.dao.TaskDao;
import com.example.moshi.dao.UserTaskDao;
import com.example.moshi.model.Task;
import com.example.moshi.util.Categories;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TaskService {

    private final TaskDao tasks = new TaskDao();
    private final UserTaskDao userTasks = new UserTaskDao();
    private final ScoreDao scores = new ScoreDao();
    private final TaskClaimDao claims = new TaskClaimDao();

    public List<TaskRow> list(Long userId) throws SQLException {
        List<TaskRow> rows = new ArrayList<>();
        Set<Long> claimed = userId == null ? Set.of() : userTasks.claimedTaskIds(userId);
        for (Task task : tasks.listActive()) {
            int done = userId == null ? 0 : scores.countRounds(userId, task.getCategory());
            int progress = Math.min(done, task.getTargetCount());
            boolean isClaimed = claimed.contains(task.getId());
            rows.add(new TaskRow(task, Categories.nameOf(task.getCategory()), progress,
                    done >= task.getTargetCount() && !isClaimed, isClaimed,
                    task.getTargetCount() == 0 ? 0 : progress * 100 / task.getTargetCount()));
        }
        return rows;
    }

    public int totalReward(Long userId) throws SQLException {
        return userId == null ? 0 : userTasks.totalReward(userId);
    }

    public TaskClaimDao.Result claim(long userId, long taskId) throws SQLException {
        return claims.claim(userId, taskId);
    }

    public static final class TaskRow {
        private final Task task;
        private final String categoryName;
        private final int progress;
        private final boolean claimable;
        private final boolean claimed;
        private final int percent;

        private TaskRow(Task task, String categoryName, int progress,
                        boolean claimable, boolean claimed, int percent) {
            this.task = task;
            this.categoryName = categoryName;
            this.progress = progress;
            this.claimable = claimable;
            this.claimed = claimed;
            this.percent = percent;
        }

        public Task getTask() { return task; }
        public String getCategoryName() { return categoryName; }
        public int getProgress() { return progress; }
        public boolean isClaimable() { return claimable; }
        public boolean isClaimed() { return claimed; }
        public int getPercent() { return percent; }
    }
}
