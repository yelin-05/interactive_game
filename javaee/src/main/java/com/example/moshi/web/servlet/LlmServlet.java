package com.example.moshi.web.servlet;

import com.example.moshi.util.Categories;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * 大模型生成（占位）。
 *
 * <p>默认 Mock：用模板拼出结构正确的题，接口形状与真实模型一致。
 * 真实调用需要 API Key，位置在 {@link #generate} 的 TODO 处。
 */
@WebServlet("/llm")
public class LlmServlet extends HttpServlet {

    private final Random random = new Random();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String topic = req.getParameter("topic");
        req.setAttribute("pageTitle", "大模型生成 · 魔识");
        req.setAttribute("active", "llm");
        req.setAttribute("categories", Categories.ALL);
        req.setAttribute("category", req.getParameter("category"));
        req.setAttribute("topic", topic);
        if (topic != null && !topic.isBlank()) {
            req.setAttribute("result", generate(topic, Categories.nameOf(req.getParameter("category"))));
        }
        req.getRequestDispatcher("/WEB-INF/views/llm.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String category = req.getParameter("category");
        String topic = req.getParameter("topic");
        String q = "?category=" + (category == null ? "computer" : category)
                + "&topic=" + java.net.URLEncoder.encode(topic == null ? "" : topic, "UTF-8");
        resp.sendRedirect(req.getContextPath() + "/llm" + q);
    }

    /** Mock 生成：模板拼题。真实实现见 TODO。 */
    private Result generate(String topic, String categoryName) {
        String t = topic == null || topic.isBlank() ? categoryName : topic.trim();
        String[] stems = {
                "关于「%s」，下列说法正确的是？",
                "下列哪一项与「%s」无关？",
                "「%s」最核心的特征是什么？"
        };
        String stem = String.format(stems[random.nextInt(stems.length)], t);
        List<String> options = List.of(
                "与主题直接相关的表述（占位选项 A）",
                "看似合理但答非所问的表述（占位选项 B）",
                "常见的混淆项（占位选项 C）",
                "完全无关的干扰项（占位选项 D）");
        String explanation = "这是一条 Mock 生成的占位解析：接入真实模型后，这里会是针对「"
                + t + "」的具体讲解，并由模型给出正确答案。";

        // TODO 真实调用：POST https://api.deepseek.com/v1/chat/completions
        //      把 prompt 发过去，解析 JSON 后映射成 Result。

        return new Result("mock", stem, options, 0, explanation);
    }

    /** 生成结果 */
    public static class Result {
        private final String mode;
        private final String question;
        private final List<String> options;
        private final int answerIndex;
        private final String explanation;

        public Result(String mode, String question, List<String> options, int answerIndex, String explanation) {
            this.mode = mode;
            this.question = question;
            this.options = options;
            this.answerIndex = answerIndex;
            this.explanation = explanation;
        }

        public String getMode() { return mode; }
        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public int getAnswerIndex() { return answerIndex; }
        public String getExplanation() { return explanation; }
    }
}
