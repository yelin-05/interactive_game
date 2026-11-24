package com.example.moshi.model;

import java.io.Serializable;
import java.util.List;

/**
 * 题目（对应 t_question），选项从 options_json 解析出来。
 */
public class Question implements Serializable {

    private long id;
    private String category;
    private String question;
    private List<String> options;
    private int answerIndex;
    private String explanation;
    private int difficulty;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getAnswerIndex() { return answerIndex; }
    public void setAnswerIndex(int answerIndex) { this.answerIndex = answerIndex; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    /** JSP 里把下标转成 A/B/C/D。 */
    public String letter(int index) {
        return String.valueOf((char) ('A' + index));
    }
}
