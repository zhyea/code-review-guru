package com.codeguru.review;

import com.codeguru.review.domain.ReviewFinding;
import com.codeguru.review.service.ReviewResult;
import com.codeguru.review.service.RuleBasedReviewer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 本地规则评审的单元测试（离线、无外部依赖）。 */
class RuleBasedReviewerTest {

    private final RuleBasedReviewer reviewer = new RuleBasedReviewer();

    @Test
    void detectsTodoAndLongLine() {
        String code = "int a = 1; // TODO 收尾\n" + "x".repeat(200) + "\n";
        ReviewResult r = reviewer.review("java", code);
        List<ReviewFinding> findings = r.getFindings();

        assertTrue(findings.stream().anyMatch(f -> "maintainability".equals(f.getCategory())));
        assertTrue(findings.stream().anyMatch(f -> ReviewFinding.SEVERITY_WARNING.equals(f.getSeverity())));
        assertNotNull(r.getScore());
        assertTrue(r.getScore() < 100);
    }

    @Test
    void detectsHardcodedSecret() {
        String code = "String password = \"supersecret123\";\n";
        ReviewResult r = reviewer.review("java", code);
        assertTrue(r.getFindings().stream().anyMatch(f -> "security".equals(f.getCategory())));
    }

    @Test
    void detectsPwdAbbreviation() {
        String code = "String pwd = \"abc123def\";\n";
        ReviewResult r = reviewer.review("java", code);
        assertTrue(r.getFindings().stream().anyMatch(f -> "疑似硬编码密钥/口令".equals(f.getTitle())));
    }

    @Test
    void detectsSqlConcat() {
        String code = "String q = \"SELECT * FROM t WHERE id = \" + id;\n";
        ReviewResult r = reviewer.review("java", code);
        assertTrue(r.getFindings().stream().anyMatch(f -> "疑似 SQL 字符串拼接".equals(f.getTitle())));
    }

    @Test
    void detectsEmptyCatch() {
        String code = "try { doIt(); } catch (Exception e) {}\n";
        ReviewResult r = reviewer.review("java", code);
        assertTrue(r.getFindings().stream().anyMatch(f -> "空的 catch 块".equals(f.getTitle())));
    }

    @Test
    void cleanCodeScores100() {
        String code = "public int add(int a, int b) {\n    return a + b;\n}\n";
        ReviewResult r = reviewer.review("java", code);
        assertTrue(r.getFindings().isEmpty());
        assertEquals(100, r.getScore());
    }
}
