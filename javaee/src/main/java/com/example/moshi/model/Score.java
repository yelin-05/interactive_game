package com.example.moshi.model;

import java.io.Serializable;

/** 一条成绩（对应 t_score）。 */
public class Score implements Serializable {

    private long id;
    private long userId;
    private String category;
    private int score;
    private int correctCount;
    private int totalCount;
    private int durationMs;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getDurationMs() { return durationMs; }
    public void setDurationMs(int durationMs) { this.durationMs = durationMs; }

    public int getDurationSeconds() {
        return durationMs / 1000;
    }
}
