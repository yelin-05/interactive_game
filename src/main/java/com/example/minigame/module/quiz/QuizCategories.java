package com.example.minigame.module.quiz;

import java.util.List;

/**
 * 题库分区定义。
 *
 * <p>分区是写死的常量而不是数据库表 —— 分区数量少且跟界面图标强绑定，
 * 放代码里改起来更直接。要新增分区，加一行这里 + 往题库 JSON 里加题即可。
 */
public final class QuizCategories {

    public record Def(String code, String name, String icon, String desc) {
    }

    public static final List<Def> ALL = List.of(
            new Def("computer", "计算机", "💻", "编程 · 网络 · 数据库"),
            new Def("literature", "文学", "📖", "中外名著 · 诗词"),
            new Def("geography", "地理", "🌏", "山川湖海 · 人文风物"),
            new Def("history", "历史", "🏛️", "中外历史大事"),
            new Def("science", "科学", "🔬", "物理 · 化学 · 生物")
    );

    private QuizCategories() {
    }

    public static boolean exists(String code) {
        return ALL.stream().anyMatch(d -> d.code().equals(code));
    }

    public static Def of(String code) {
        return ALL.stream().filter(d -> d.code().equals(code)).findFirst().orElse(null);
    }
}
