package com.codeguru.review.domain;

import java.time.LocalDateTime;

/**
 * 一次代码评审请求的持久化记录。
 *
 * <p>{@code reviewerType} 取值 {@code AI}（大模型评审）或 {@code RULE}（本地规则评审/降级）。
 * {@code score} 为 0–100 的综合评分，规则评审按问题严重度折算，AI 评审取模型返回值。</p>
 */
public class ReviewRequest {

    public static final String TYPE_AI = "AI";
    public static final String TYPE_RULE = "RULE";
    public static final String STATUS_DONE = "DONE";

    private Long id;
    private String title;
    private String language;
    private String sourceCode;
    private int sourceSize;
    private String reviewerType;
    private String model;
    private String summary;
    private Integer score;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public int getSourceSize() { return sourceSize; }
    public void setSourceSize(int sourceSize) { this.sourceSize = sourceSize; }

    public String getReviewerType() { return reviewerType; }
    public void setReviewerType(String reviewerType) { this.reviewerType = reviewerType; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
