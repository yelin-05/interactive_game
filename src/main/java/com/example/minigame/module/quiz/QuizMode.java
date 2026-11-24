package com.example.minigame.module.quiz;

import java.util.List;

/**
 * 答题难度档位。
 *
 * <p>档位决定一局「抽多少题、每题多少秒、只抽哪些难度的题」，
 * 三档写死在这里，与界面难度选择一一对应。
 */
public enum QuizMode {

    EASY("easy", "简单", "🌱", "5 题 · 每题 20 秒 · 只出简单题", 5, 20, List.of(1)),
    NORMAL("normal", "普通", "⚡", "7 题 · 每题 15 秒 · 简单 + 中等", 7, 15, List.of(1, 2)),
    HARD("hard", "困难", "🔥", "10 题 · 每题 10 秒 · 中等 + 困难", 10, 10, List.of(2, 3));

    private final String code;
    private final String label;
    private final String icon;
    private final String desc;
    private final int count;
    private final int secondsPerQuestion;
    private final List<Integer> difficulties;

    QuizMode(String code, String label, String icon, String desc,
             int count, int secondsPerQuestion, List<Integer> difficulties) {
        this.code = code;
        this.label = label;
        this.icon = icon;
        this.desc = desc;
        this.count = count;
        this.secondsPerQuestion = secondsPerQuestion;
        this.difficulties = difficulties;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public String icon() {
        return icon;
    }

    public String desc() {
        return desc;
    }

    public int count() {
        return count;
    }

    public int secondsPerQuestion() {
        return secondsPerQuestion;
    }

    public List<Integer> difficulties() {
        return difficulties;
    }

    /** 按 code 解析，非法/为空回退到普通档。 */
    public static QuizMode from(String code) {
        if (code == null || code.isBlank()) {
            return NORMAL;
        }
        for (QuizMode m : values()) {
            if (m.code.equalsIgnoreCase(code)) {
                return m;
            }
        }
        return NORMAL;
    }

    public static List<QuizMode> all() {
        return List.of(values());
    }

    /**
     * 难度分档基础分：简单 10 / 中等 20 / 困难 30。
     * 这是「分档计分」的核心，答对一题先拿这个基础分。
     */
    public static int baseScore(Integer difficulty) {
        int d = difficulty == null ? 1 : difficulty;
        return d >= 3 ? 30 : (d == 2 ? 20 : 10);
    }
}
