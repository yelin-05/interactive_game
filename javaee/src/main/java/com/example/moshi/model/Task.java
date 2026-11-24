package com.example.moshi.model;

import java.io.Serializable;

/** 任务（对应 t_task）。 */
public class Task implements Serializable {

    private long id;
    private String title;
    private String description;
    private String category;
    private int targetCount;
    private int rewardPoints;
    private int status;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getTargetCount() { return targetCount; }
    public void setTargetCount(int targetCount) { this.targetCount = targetCount; }
    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
