package com.example.moshi.web.servlet;

import com.example.moshi.dao.ScoreDao;
import com.example.moshi.dao.TaskDao;
import com.example.moshi.dao.UserTaskDao;
import com.example.moshi.model.Task;
import com.example.moshi.util.Categories;
import com.example.moshi.web.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 任务中心：进度按成绩表实时计算，领取后写 t_user_task。
 */
@WebServlet("/tasks")
public class TasksServlet extends HttpServlet {

    private final TaskDao taskDao = new TaskDao();
    private final UserTaskDao userTaskDao = new UserTaskDao();
    private final ScoreDao scoreDao = new ScoreDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(SessionKeys.USER_ID);

        List<Map<String, Object>> rows = new ArrayList<>();
        int totalReward = 0;
        try {
            List<Task> tasks = taskDao.listActive();
            Set<Long> claimed = userId == null ? Set.of() : userTaskDao.claimedTaskIds(userId);
            if (userId != null) {
                totalReward = userTaskDao.totalReward(userId);
            }
            for (Task t : tasks) {
                int done = userId == null ? 0 : scoreDao.countRounds(userId, t.getCategory());
                int progress = Math.min(done, t.getTargetCount());
                boolean isClaimed = claimed.contains(t.getId());
                boolean claimable = done >= t.getTargetCount() && !isClaimed;

                Map<String, Object> row = new HashMap<>();
                row.put("task", t);
                row.put("categoryName", Categories.nameOf(t.getCategory()));
                row.put("progress", progress);
                row.put("claimable", claimable);
                row.put("claimed", isClaimed);
                row.put("percent", t.getTargetCount() == 0 ? 0 : progress * 100 / t.getTargetCount());
                rows.add(row);
            }
        } catch (Exception e) {
            req.setAttribute("error", "读取任务失败：" + e.getMessage());
        }

        req.setAttribute("pageTitle", "任务中心 · 魔识");
        req.setAttribute("active", "tasks");
        req.setAttribute("rows", rows);
        req.setAttribute("totalReward", totalReward);
        req.setAttribute("loggedIn", userId != null);
        req.setAttribute("claimedPoints", req.getParameter("claimed"));
        req.getRequestDispatcher("/WEB-INF/views/tasks.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(SessionKeys.USER_ID);
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login?need=1&to=/tasks");
            return;
        }
        long taskId;
        try {
            taskId = Long.parseLong(req.getParameter("taskId"));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/tasks?error=参数错误");
            return;
        }

        try {
            Task task = taskDao.findById(taskId);
            if (task == null || task.getStatus() == 0) {
                resp.sendRedirect(req.getContextPath() + "/tasks?error=任务不存在或已下架");
                return;
            }
            if (userTaskDao.exists(userId, taskId)) {
                resp.sendRedirect(req.getContextPath() + "/tasks?error=这个任务的奖励已经领过了");
                return;
            }
            int done = scoreDao.countRounds(userId, task.getCategory());
            if (done < task.getTargetCount()) {
                resp.sendRedirect(req.getContextPath() + "/tasks?error=还没完成，还差 "
                        + (task.getTargetCount() - done) + " 局");
                return;
            }
            userTaskDao.insert(userId, taskId, task.getRewardPoints());
            resp.sendRedirect(req.getContextPath() + "/tasks?claimed=" + task.getRewardPoints());
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/tasks?error=领取失败：" + e.getMessage());
        }
    }
}
