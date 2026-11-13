package com.codeguru.review.service;

import com.codeguru.review.domain.ReviewFinding;
import com.codeguru.review.domain.ReviewRequest;
import com.codeguru.review.mapper.ReviewFindingMapper;
import com.codeguru.review.mapper.ReviewRequestMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审编排与持久化：选择评审策略（AI / 本地规则），落库评审请求与发现，返回 {@link ReviewOutcome}。
 *
 * <p>策略选择：仅当存在可用的 {@link ChatClient.Builder} 且配置了 API Key 时才走 AI；
 * 否则（含未配置、调用异常）使用 {@link RuleBasedReviewer}，异常时在摘要里显性标注降级。</p>
 */
@Service
public class CodeReviewService {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewService.class);

    private static final int MAX_CODE_LEN = 200_000;
    /** application.yml 里的占位 key：仅供 spring-ai 各模型 Bean 构造期校验，非真实凭据。 */
    private static final String PLACEHOLDER_KEY = "sk-local-placeholder";

    private final ReviewRequestMapper requestMapper;
    private final ReviewFindingMapper findingMapper;
    private final RuleBasedReviewer ruleBased;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilder;
    private final String aiModel;
    private final String openAiKey;
    private final String aiEnabledProp;

    public CodeReviewService(ReviewRequestMapper requestMapper,
                             ReviewFindingMapper findingMapper,
                             RuleBasedReviewer ruleBased,
                             ObjectProvider<ChatClient.Builder> chatClientBuilder,
                             @Value("${app.review.ai-model:gpt-4o-mini}") String aiModel,
                             @Value("${spring.ai.openai.api-key:}") String openAiKey,
                             @Value("${app.review.ai-enabled:auto}") String aiEnabledProp) {
        this.requestMapper = requestMapper;
        this.findingMapper = findingMapper;
        this.ruleBased = ruleBased;
        this.chatClientBuilder = chatClientBuilder;
        this.aiModel = aiModel;
        this.openAiKey = openAiKey;
        this.aiEnabledProp = aiEnabledProp;
    }

    /** 提交评审：校验在控制器完成，这里假定入参已合规。 */
    public ReviewOutcome review(String title, String language, String code) {
        ReviewResult result;
        String reviewerType;
        String model = null;

        ChatClient.Builder builder = chatClientBuilder.getIfAvailable();
        boolean aiConfigured = builder != null && aiEnabled();
        if (aiConfigured) {
            try {
                AiReviewer ai = new AiReviewer(builder.build(), aiModel);
                result = ai.review(language, code);
                reviewerType = ReviewRequest.TYPE_AI;
                model = aiModel;
            } catch (Exception e) {
                // AI 调用失败 → 降级本地规则。异常只进服务端日志，不回显给页面：
                // 原始消息可能带 base-url / key 片段 / 内部类名，落到 summary 会泄露内部信息。
                log.warn("AI 评审调用失败，降级为本地规则评审", e);
                result = ruleBased.review(language, code);
                result.setSummary("[AI 调用失败，已降级为本地规则评审] " + result.getSummary());
                reviewerType = ReviewRequest.TYPE_RULE;
            }
        } else {
            result = ruleBased.review(language, code);
            reviewerType = ReviewRequest.TYPE_RULE;
        }

        ReviewRequest req = new ReviewRequest();
        req.setTitle(title);
        req.setLanguage(language);
        req.setSourceCode(code);
        req.setSourceSize(code.length());
        req.setReviewerType(reviewerType);
        req.setModel(model);
        req.setSummary(result.getSummary());
        req.setScore(result.getScore());
        req.setStatus(ReviewRequest.STATUS_DONE);
        req.setCreatedAt(LocalDateTime.now());
        requestMapper.insert(req);

        List<ReviewFinding> findings = result.getFindings();
        for (ReviewFinding f : findings) {
            f.setRequestId(req.getId());
            f.setCreatedAt(LocalDateTime.now());
            findingMapper.insert(f);
        }

        ReviewOutcome outcome = new ReviewOutcome();
        outcome.setRequest(req);
        outcome.setFindings(findings);
        return outcome;
    }

    public ReviewRequest getRequest(long id) {
        return requestMapper.findById(id);
    }

    public List<ReviewFinding> getFindings(long requestId) {
        return findingMapper.listByRequestId(requestId);
    }

    public List<ReviewRequest> recent(int limit, long offset) {
        return requestMapper.listRecent(limit, offset);
    }

    public long count() {
        return requestMapper.count();
    }

    public static int maxCodeLength() {
        return MAX_CODE_LEN;
    }

    /**
     * 是否启用 AI 评审：默认 auto —— 仅当配置了真实（非占位）key 时才启用；
     * {@code app.review.ai-enabled=false} 强制关闭，{@code true} 在有真实 key 时等价于 auto。
     */
    private boolean aiEnabled() {
        if ("false".equalsIgnoreCase(aiEnabledProp)) {
            return false;
        }
        return openAiKey != null && !openAiKey.isBlank() && !PLACEHOLDER_KEY.equals(openAiKey);
    }
}
