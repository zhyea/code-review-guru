package com.codeguru.review.service;

import com.codeguru.review.domain.ReviewFinding;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 评审：基于 spring-ai 的 {@link ChatClient} 调用大模型，要求模型返回结构化 JSON，
 * 解析为 {@link ReviewResult}。解析失败时降级为单条 INFO（保留原文），不整次失败。
 *
 * <p>本类不注册为 Spring Bean：仅当检测到可用的 {@link ChatClient.Builder} 时才由
 * {@link CodeReviewService} 手动实例化，从而在未配置 AI 密钥时不影响应用启动。</p>
 */
public class AiReviewer {

    private static final String SYSTEM = """
            你是一位资深的代码评审专家。请对给定的代码片段进行评审，并以严格的 JSON 对象返回，\
            不要包含任何解释性文字或 markdown 代码围栏。JSON 结构如下：
            {
              "summary": "不超过 200 字的整体评价，指出主要风险与亮点",
              "score": 0 到 100 之间的整数（代码质量综合评分，越高越好）,
              "findings": [
                {
                  "severity": "ERROR" | "WARNING" | "INFO",
                  "category": "bugs|security|performance|style|maintainability|other",
                  "line": 行号整数或 null,
                  "title": "一句话问题标题",
                  "detail": "问题描述与依据",
                  "suggestion": "具体修改建议（可含示例）"
                }
              ]
            }
            评审要点：正确性/边界条件、安全漏洞（注入/越权/硬编码密钥）、性能、可读性、错误处理、\
            并发与资源管理。只报告确有依据的问题，避免误报；line 尽量对应代码中的实际行号。""";

    private final ChatClient chatClient;
    private final String model;

    public AiReviewer(ChatClient chatClient, String model) {
        this.chatClient = chatClient;
        this.model = model;
    }

    public ReviewResult review(String language, String code) {
        String raw = chatClient.prompt()
                .system(SYSTEM)
                .user("编程语言：" + language + "\n\n待评审代码：\n```\n" + code + "\n```")
                .options(OpenAiChatOptions.builder().model(model).temperature(0.1).build())
                .call()
                .content();
        return parse(raw);
    }

    private ReviewResult parse(String raw) {
        String json = extractJson(raw);
        ObjectMapper om = new ObjectMapper();
        try {
            AiResponse resp = om.readValue(json, AiResponse.class);
            List<ReviewFinding> list = new ArrayList<>();
            if (resp.findings != null) {
                for (AiFinding af : resp.findings) {
                    ReviewFinding f = new ReviewFinding();
                    f.setSeverity(normalizeSeverity(af.severity));
                    f.setCategory(af.category);
                    f.setLine(af.line);
                    f.setTitle(af.title == null ? "(无标题)" : af.title);
                    f.setDetail(af.detail);
                    f.setSuggestion(af.suggestion);
                    list.add(f);
                }
            }
            String summary = resp.summary == null ? "" : resp.summary;
            return new ReviewResult(summary, resp.score, list);
        } catch (Exception e) {
            ReviewFinding f = new ReviewFinding();
            f.setSeverity(ReviewFinding.SEVERITY_INFO);
            f.setCategory("raw");
            f.setTitle("AI 返回内容（解析失败，已原样保留）");
            f.setDetail(raw == null ? "" : raw);
            return new ReviewResult("AI 返回内容无法解析为结构化 JSON，已原样保留以便查看。", 0, List.of(f));
        }
    }

    private static String extractJson(String raw) {
        if (raw == null) {
            return "{}";
        }
        String s = raw.trim();
        if (s.startsWith("```")) {
            int firstNewline = s.indexOf('\n');
            if (firstNewline >= 0) {
                s = s.substring(firstNewline + 1);
            }
            int lastFence = s.lastIndexOf("```");
            if (lastFence >= 0) {
                s = s.substring(0, lastFence);
            }
            s = s.trim();
        }
        return s;
    }

    private static String normalizeSeverity(String s) {
        if (s == null) {
            return ReviewFinding.SEVERITY_INFO;
        }
        String u = s.trim().toUpperCase();
        if (u.contains("ERROR") || u.equals("错误")) {
            return ReviewFinding.SEVERITY_ERROR;
        }
        if (u.contains("WARN") || u.equals("警告")) {
            return ReviewFinding.SEVERITY_WARNING;
        }
        return ReviewFinding.SEVERITY_INFO;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AiResponse {
        public String summary;
        public Integer score;
        public List<AiFinding> findings;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AiFinding {
        public String severity;
        public String category;
        public Integer line;
        public String title;
        public String detail;
        public String suggestion;
    }
}
