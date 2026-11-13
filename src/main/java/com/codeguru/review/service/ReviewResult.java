package com.codeguru.review.service;

import com.codeguru.review.domain.ReviewFinding;

import java.util.List;

/**
 * 一次评审产出的中间结果（尚未落库）：综合摘要、0–100 评分、结构化问题列表。
 * 本地规则评审与 AI 评审都返回该结构，便于统一落库。
 */
public class ReviewResult {

    private String summary;
    private Integer score;
    private List<ReviewFinding> findings;

    public ReviewResult() {
    }

    public ReviewResult(String summary, Integer score, List<ReviewFinding> findings) {
        this.summary = summary;
        this.score = score;
        this.findings = findings;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public List<ReviewFinding> getFindings() { return findings; }
    public void setFindings(List<ReviewFinding> findings) { this.findings = findings; }
}
