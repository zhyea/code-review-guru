package com.codeguru.review.service;

import com.codeguru.review.domain.ReviewFinding;
import com.codeguru.review.domain.ReviewRequest;

import java.util.List;

/** 评审落库后的完整产出：评审请求记录 + 其下的问题列表。 */
public class ReviewOutcome {

    private ReviewRequest request;
    private List<ReviewFinding> findings;

    public ReviewRequest getRequest() { return request; }
    public void setRequest(ReviewRequest request) { this.request = request; }

    public List<ReviewFinding> getFindings() { return findings; }
    public void setFindings(List<ReviewFinding> findings) { this.findings = findings; }
}
