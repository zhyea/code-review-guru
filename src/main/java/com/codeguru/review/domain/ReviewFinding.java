package com.codeguru.review.domain;

import java.time.LocalDateTime;

/**
 * 评审发现的一条问题。
 *
 * <p>{@code severity} 取值 {@code ERROR} / {@code WARNING} / {@code INFO}（模板据此着色）。
 * {@code line} 为命中行号，可空；{@code category} 为问题分类（如 security / style / bugs）。</p>
 */
public class ReviewFinding {

    public static final String SEVERITY_ERROR = "ERROR";
    public static final String SEVERITY_WARNING = "WARNING";
    public static final String SEVERITY_INFO = "INFO";

    private Long id;
    private Long requestId;
    private String severity;
    private String category;
    private Integer line;
    private String title;
    private String detail;
    private String suggestion;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getLine() { return line; }
    public void setLine(Integer line) { this.line = line; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
