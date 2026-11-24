package com.example.moshi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 一局答题的状态，整局存在 HttpSession 里。
 *
 * <p>正确答案随题目一起放在服务端，页面只拿到题干和选项 ——
 * 前端看不到 answerIndex，交卷后由服务端判分。
 */
public class QuizRound implements Serializable {

    /** 单题：下发页面的字段（不含答案） */
    public static class Item implements Serializable {
        private final long id;
        private final String question;
        private final List<String> options;
        private final int difficulty;
        private final int answerIndex;
        private final String explanation;

        public Item(long id, String question, List<String> options,
                    int difficulty, int answerIndex, String explanation) {
            this.id = id;
            this.question = question;
            this.options = options;
            this.difficulty = difficulty;
            this.answerIndex = answerIndex;
            this.explanation = explanation;
        }

        public long getId() { return id; }
        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public int getDifficulty() { return difficulty; }
        public int getAnswerIndex() { return answerIndex; }
        public String getExplanation() { return explanation; }
    }

    /** 判分结果的一行 */
    public static class Detail implements Serializable {
        private final String question;
        private final int correctAnswer;
        private final int yourAnswer;
        private final boolean correct;
        private final String explanation;

        public Detail(String question, int correctAnswer, int yourAnswer,
                      boolean correct, String explanation) {
            this.question = question;
            this.correctAnswer = correctAnswer;
            this.yourAnswer = yourAnswer;
            this.correct = correct;
            this.explanation = explanation;
        }

        public String getQuestion() { return question; }
        public int getCorrectAnswer() { return correctAnswer; }
        public int getYourAnswer() { return yourAnswer; }
        public boolean isCorrect() { return correct; }
        public String getExplanation() { return explanation; }

        public String getYourLetter() {
            return yourAnswer < 0 ? "未作答" : String.valueOf((char) ('A' + yourAnswer));
        }

        public String getCorrectLetter() {
            return String.valueOf((char) ('A' + correctAnswer));
        }
    }

    /** 结算结果 */
    public static class Result implements Serializable {
        private final int score;
        private final int baseScore;
        private final int speedBonus;
        private final int correct;
        private final int total;
        private final int durationMs;
        private final boolean saved;
        private final List<Detail> details;

        public Result(int score, int baseScore, int speedBonus, int correct, int total,
                      int durationMs, boolean saved, List<Detail> details) {
            this.score = score;
            this.baseScore = baseScore;
            this.speedBonus = speedBonus;
            this.correct = correct;
            this.total = total;
            this.durationMs = durationMs;
            this.saved = saved;
            this.details = details;
        }

        public int getScore() { return score; }
        public int getBaseScore() { return baseScore; }
        public int getSpeedBonus() { return speedBonus; }
        public int getCorrect() { return correct; }
        public int getTotal() { return total; }
        public int getDurationMs() { return durationMs; }
        public boolean isSaved() { return saved; }
        public List<Detail> getDetails() { return details; }
        public int getDurationSeconds() { return durationMs / 1000; }
    }

    private final String category;
    private final String modeCode;
    private final int secondsPerQuestion;
    private final List<Item> items;
    private final int[] answers;
    private final long startedAt;
    private int index;

    public QuizRound(String category, QuizMode mode, List<Item> items, long startedAt) {
        this.category = category;
        this.modeCode = mode.getCode();
        this.secondsPerQuestion = mode.getSecondsPerQuestion();
        this.items = items;
        this.answers = new int[items.size()];
        for (int i = 0; i < answers.length; i++) {
            answers[i] = -1;
        }
        this.startedAt = startedAt;
        this.index = 0;
    }

    public String getCategory() { return category; }
    public String getModeCode() { return modeCode; }
    public int getSecondsPerQuestion() { return secondsPerQuestion; }
    public List<Item> getItems() { return items; }
    public int[] getAnswers() { return answers; }
    public long getStartedAt() { return startedAt; }
    public int getIndex() { return index; }

    public boolean isFinished() {
        return index >= items.size();
    }

    public Item current() {
        return isFinished() ? null : items.get(index);
    }

    public void answer(int pick) {
        answers[index] = pick;
        index++;
    }

    public int getAnsweredCount() {
        int n = 0;
        for (int a : answers) {
            if (a >= 0) {
                n++;
            }
        }
        return n;
    }

    /** 服务端判分：按难度给基础分 + 提前完成的时间奖励（上限 50）。 */
    public Result grade(boolean saved) {
        int correct = 0;
        List<Detail> details = new ArrayList<>();
        int base = 0;
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            int pick = answers[i];
            boolean ok = pick == it.getAnswerIndex();
            if (ok) {
                correct++;
                base += QuizMode.baseScore(it.getDifficulty());
            }
            details.add(new Detail(it.getQuestion(), it.getAnswerIndex(), pick, ok, it.getExplanation()));
        }

        int durationMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startedAt);
        int limitSec = items.size() * secondsPerQuestion;
        int elapsedSec = durationMs / 1000;
        int speedBonus = Math.max(0, Math.min(50, (limitSec - elapsedSec) * 2));

        return new Result(base + speedBonus, base, speedBonus, correct, items.size(),
                durationMs, saved, details);
    }
}
