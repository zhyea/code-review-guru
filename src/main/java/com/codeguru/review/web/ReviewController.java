package com.codeguru.review.web;

import com.codeguru.review.domain.ReviewFinding;
import com.codeguru.review.domain.ReviewRequest;
import com.codeguru.review.service.CodeReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评审平台页面与提交入口（服务端渲染）。
 *
 * <p>页面：首页（提交表单 + 最近评审）、历史列表、详情。提交走 {@code POST /reviews}，
 * 由 {@link CodeReviewService} 完成评审与落库后 302 跳转详情页。</p>
 */
@Controller
@RequestMapping("/")
public class ReviewController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int RECENT_LIMIT = 12;
    private static final int LIST_SIZE = 20;

    private static final List<String> LANGUAGES = List.of(
            "java", "kotlin", "python", "javascript", "typescript",
            "go", "rust", "cpp", "csharp", "sql", "shell", "other");

    private final CodeReviewService service;

    public ReviewController(CodeReviewService service) {
        this.service = service;
    }

    /** 所有视图共享的语言下拉项。 */
    @ModelAttribute("languages")
    public List<String> languages() {
        return LANGUAGES;
    }

    @GetMapping
    public String home(Model model,
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String titleValue,
                       @RequestParam(required = false) String language,
                       @RequestParam(required = false) String codeValue) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        model.addAttribute("titleValue", titleValue == null ? "" : titleValue);
        model.addAttribute("language", language == null ? "java" : language);
        model.addAttribute("codeValue", codeValue == null ? "" : codeValue);
        model.addAttribute("recent", recentRows(RECENT_LIMIT, 0));
        return "index";
    }

    @PostMapping("/reviews")
    public String submit(@RequestParam(required = false) String title,
                         @RequestParam String language,
                         @RequestParam String code,
                         Model model) {
        if (code == null || code.isBlank()) {
            return renderError(model, "代码不能为空", title, language, code);
        }
        if (code.length() > CodeReviewService.maxCodeLength()) {
            return renderError(model, "代码过长（上限 " + CodeReviewService.maxCodeLength() + " 字符）",
                    title, language, code);
        }
        if (!LANGUAGES.contains(language)) {
            language = "other";
        }
        String safeTitle = (title == null || title.isBlank())
                ? ("未命名评审（" + language + "）")
                : title.trim().substring(0, Math.min(title.length(), 200));

        var outcome = service.review(safeTitle, language, code);
        return "redirect:/reviews/" + outcome.getRequest().getId();
    }

    @GetMapping("/reviews")
    public String list(Model model, @RequestParam(defaultValue = "0") int page) {
        int p = Math.max(0, page);
        long offset = (long) p * LIST_SIZE;
        List<ReviewRow> rows = service.recent(LIST_SIZE, offset).stream()
                .map(this::toRow)
                .collect(Collectors.toList());
        model.addAttribute("reviews", rows);
        model.addAttribute("page", p);
        model.addAttribute("hasMore", service.count() > offset + LIST_SIZE);
        return "reviews";
    }

    @GetMapping("/reviews/{id}")
    public String detail(@PathVariable long id, Model model) {
        ReviewRequest req = service.getRequest(id);
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<ReviewFinding> findings = service.getFindings(id);
        model.addAttribute("req", req);
        model.addAttribute("createdAtText",
                req.getCreatedAt() == null ? "" : req.getCreatedAt().format(FMT));
        model.addAttribute("findings", findings);
        return "review-detail";
    }

    private String renderError(Model model, String error, String title, String language, String code) {
        model.addAttribute("error", error);
        model.addAttribute("titleValue", title == null ? "" : title);
        model.addAttribute("language",
                language == null ? "java" : (LANGUAGES.contains(language) ? language : "other"));
        model.addAttribute("codeValue", code == null ? "" : code);
        model.addAttribute("recent", recentRows(RECENT_LIMIT, 0));
        return "index";
    }

    private List<ReviewRow> recentRows(int limit, long offset) {
        return service.recent(limit, offset).stream().map(this::toRow).collect(Collectors.toList());
    }

    private ReviewRow toRow(ReviewRequest r) {
        ReviewRow row = new ReviewRow();
        row.setId(r.getId());
        row.setTitle(r.getTitle());
        row.setLanguage(r.getLanguage());
        row.setReviewerType(r.getReviewerType());
        row.setScoreText(r.getScore() == null ? null : String.valueOf(r.getScore()));
        row.setCreatedAtText(r.getCreatedAt() == null ? "" : r.getCreatedAt().format(FMT));
        return row;
    }

    /** 列表行视图对象（时间在控制器里格式化成字符串，规避 FreeMarker 对 java.time 的 ?string 500）。 */
    public static final class ReviewRow {
        private long id;
        private String title;
        private String language;
        private String reviewerType;
        private String scoreText;
        private String createdAtText;

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getReviewerType() { return reviewerType; }
        public void setReviewerType(String reviewerType) { this.reviewerType = reviewerType; }
        public String getScoreText() { return scoreText; }
        public void setScoreText(String scoreText) { this.scoreText = scoreText; }
        public String getCreatedAtText() { return createdAtText; }
        public void setCreatedAtText(String createdAtText) { this.createdAtText = createdAtText; }
    }
}
