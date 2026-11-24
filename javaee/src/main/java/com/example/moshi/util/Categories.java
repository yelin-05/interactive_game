package com.example.moshi.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 题库的五个分区。
 *
 * <p>分区数量少、图标固定，写成常量比建表更简单；
 * 新增分区只要在这里加一条，再往 t_question 里插题即可。
 */
public final class Categories {

    public static class Def {
        private final String code;
        private final String name;
        private final String icon;
        private final String desc;

        public Def(String code, String name, String icon, String desc) {
            this.code = code;
            this.name = name;
            this.icon = icon;
            this.desc = desc;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getIcon() { return icon; }
        public String getDesc() { return desc; }
    }

    public static final List<Def> ALL = new ArrayList<>();

    static {
        ALL.add(new Def("computer", "计算机", "💻", "编程 · 网络 · 数据库"));
        ALL.add(new Def("literature", "文学", "📖", "中外名著 · 诗词"));
        ALL.add(new Def("geography", "地理", "🌏", "山川湖海 · 人文风物"));
        ALL.add(new Def("history", "历史", "🏛️", "中外历史大事"));
        ALL.add(new Def("science", "科学", "🔬", "物理 · 化学 · 生物"));
    }

    private Categories() {
    }

    public static boolean exists(String code) {
        return code != null && ALL.stream().anyMatch(d -> d.getCode().equals(code));
    }

    public static Def of(String code) {
        return ALL.stream().filter(d -> d.getCode().equals(code)).findFirst().orElse(null);
    }

    public static String nameOf(String code) {
        Def d = of(code);
        return d == null ? (code == null ? "任意分区" : code) : d.getName();
    }
}
