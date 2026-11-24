package com.example.moshi.model;

import java.io.Serializable;

/** 排行榜一行（成绩 + 昵称，来自两表连接）。 */
public class RankRow implements Serializable {

    private int rank;
    private String nickname;
    private int score;
    private int correct;
    private int total;
    private int durationSeconds;
    private String createdAt;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getCorrect() { return correct; }
    public void setCorrect(int correct) { this.correct = correct; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
