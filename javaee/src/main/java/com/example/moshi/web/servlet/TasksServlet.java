package com.example.moshi.web.servlet;

import com.example.moshi.dao.TaskClaimDao;
import com.example.moshi.service.TaskService;
import com.example.moshi.web.SessionKeys;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@WebServlet("/tasks")
public class TasksServlet extends HttpServlet {

    private final TaskService tasks = new TaskService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(SessionKeys.USER_ID);
        try {
            req.setAttribute("rows", tasks.list(userId));
            req.setAttribute("totalReward", tasks.totalReward(userId));
        } catch (Exception ex) {
            req.setAttribute("rows", List.of());
            req.setAttribute("totalReward", 0);
            req.setAttribute("error", "任务服务暂不可用，请稍后重试");
        }
        req.setAttribute("pageTitle", "任务中心 · 魔识");
        req.setAttribute("active", "tasks");
        req.setAttribute("loggedIn", userId != null);
        req.setAttribute("claimedPoints", nonNegativeInt(req.getParameter("claimed")));
        if (req.getParameter("error") != null) {
            req.setAttribute("error", req.getParameter("error"));
        }
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
        final long taskId;
        try {
            taskId = Long.parseLong(req.getParameter("taskId"));
        } catch (Exception ex) {
            redirect(req, resp, "参数错误");
            return;
        }
        try {
            TaskClaimDao.Result result = tasks.claim(userId, taskId);
            switch (result.getStatus()) {
                case CLAIMED:
                    resp.sendRedirect(req.getContextPath() + "/tasks?claimed=" + result.getRewardPoints());
                    break;
                case ALREADY_CLAIMED:
                    redirect(req, resp, "这个任务的奖励已经领过了");
                    break;
                case NOT_FINISHED:
                    redirect(req, resp, "还没完成，还差 " + result.getRemaining() + " 局");
                    break;
                default:
                    redirect(req, resp, "任务不存在或已下架");
            }
        } catch (Exception ex) {
            redirect(req, resp, "领取失败，请稍后重试");
        }
    }

    private Integer nonNegativeInt(String value) {
        try {
            int number = Integer.parseInt(value);
            return number >= 0 ? number : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private void redirect(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/tasks?error=" + URLEncoder.encode(message, "UTF-8"));
    }
}
