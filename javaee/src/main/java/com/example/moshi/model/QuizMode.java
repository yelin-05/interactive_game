package com.example.moshi.model;

/**
 * 难度档位：决定一局抽多少题、每题多少秒、只抽哪些难度。
 *
 * <p>三档写死，和页面的难度选择一一对应。
 */
public enum QuizMode {

    EASY("easy", "简单", "🌱", "5 题 · 每题 20 秒 · 只出简单题", 5, 20, new int[]{1}),
    NORMAL("normal", "普通", "⚡", "7 题 · 每题 15 秒 · 简单 + 中等", 7, 15, new int[]{1, 2}),
    HARD("hard", "困难", "🔥", "10 题 · 每题 10 秒 · 中等 + 困难", 10, 10, new int[]{2, 3});

    private final String code;
    private final String label;
    private final String icon;
    private final String desc;
    private final int count;
    private final int secondsPerQuestion;
    private final int[] difficulties;

    QuizMode(String code, String label, String icon, String desc,
             int count, int secondsPerQuestion, int[] difficulties) {
        this.code = code;
        this.label = label;
        this.icon = icon;
        this.desc = desc;
        this.count = count;
        this.secondsPerQuestion = secondsPerQuestion;
        this.difficulties = difficulties;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getIcon() { return icon; }
    public String getDesc() { return desc; }
    public int getCount() { return count; }
    public int getSecondsPerQuestion() { return secondsPerQuestion; }
    public int[] getDifficulties() { return difficulties; }

    public static QuizMode from(String code) {
        if (code != null) {
            for (QuizMode m : values()) {
                if (m.code.equalsIgnoreCase(code)) {
                    return m;
                }
            }
        }
        return NORMAL;
    }

    /** 分档基础分：简单 10 / 中等 20 / 困难 30。 */
    public static int baseScore(int difficulty) {
        return difficulty >= 3 ? 30 : (difficulty == 2 ? 20 : 10);
    }
}
