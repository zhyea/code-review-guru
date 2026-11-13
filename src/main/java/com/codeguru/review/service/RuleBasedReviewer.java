package com.codeguru.review.service;

import com.codeguru.review.domain.ReviewFinding;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 本地规则评审：不依赖任何外部服务，对代码做基础静态检查。
 *
 * <p>用于两种场景：① 未配置 AI 密钥时的默认评审方式；② AI 调用失败时的降级兜底。
 * 检查项均为语言无关的简单启发式，命中后产出结构化 {@link ReviewFinding} 并折算综合评分。</p>
 */
@Component
public class RuleBasedReviewer {

    private static final int MAX_LINE_LEN = 120;

    private static final Pattern TODO = Pattern.compile("(?i)\\b(TODO|FIXME|XXX|HACK)\\b");
    private static final Pattern PRINT_STACK = Pattern.compile("(?i)\\.printStackTrace\\s*\\(");
    private static final Pattern SYSOUT = Pattern.compile("(?i)\\bSystem\\.(out|err)\\.print");
    private static final Pattern SQL_LITERAL = Pattern.compile(
            "\"[^\"]*\\b(SELECT|INSERT|UPDATE|DELETE|DROP|UNION)\\b[^\"]*\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern HARDCODED_SECRET = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|api[_-]?key|access[_-]?key|token|credentials?)\\s*[:=]\\s*[\"'][^\"']{3,}[\"']");
    private static final Pattern EMPTY_CATCH = Pattern.compile("catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}");

    public ReviewResult review(String language, String code) {
        List<ReviewFinding> findings = new ArrayList<>();
        String[] lines = code.split("\\r?\\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String ln = lines[i];
            int num = i + 1;

            if (ln.length() > MAX_LINE_LEN) {
                add(findings, ReviewFinding.SEVERITY_WARNING, "style", num,
                        "单行过长（" + ln.length() + " 字符）",
                        "该行超过 " + MAX_LINE_LEN + " 字符，可读性差，且容易在合并/审阅时引入缺陷。",
                        "拆分为多行，或提取为具名变量/方法。");
            }
            if (TODO.matcher(ln).find()) {
                add(findings, ReviewFinding.SEVERITY_INFO, "maintainability", num,
                        "存在待办标记（TODO/FIXME 等）",
                        "代码里遗留了未完成的标记，建议跟踪处理或补充 Issue 链接。",
                        "明确处理计划，或移除临时标记。");
            }
            if (PRINT_STACK.matcher(ln).find()) {
                add(findings, ReviewFinding.SEVERITY_WARNING, "logging", num,
                        "直接打印异常栈",
                        "printStackTrace() 把异常打到标准错误，生产环境难以归集与监控，且会泄露内部信息。",
                        "改用日志框架（如 SLF4J）记录，并带上上下文。");
            }
            if (SYSOUT.matcher(ln).find()) {
                add(findings, ReviewFinding.SEVERITY_INFO, "logging", num,
                        "使用 System.out/err 输出",
                        "直接用标准输出绕过了日志框架，无法控制级别与落盘。",
                        "统一改用日志框架输出。");
            }
            if (ln.indexOf('+') >= 0 && SQL_LITERAL.matcher(ln).find()) {
                add(findings, ReviewFinding.SEVERITY_WARNING, "security", num,
                        "疑似 SQL 字符串拼接",
                        "通过字符串拼接构造 SQL，用户输入未转义时容易引入 SQL 注入。",
                        "改用参数化查询 / 预编译语句（PreparedStatement 或 ORM 占位符）。");
            }
            if (HARDCODED_SECRET.matcher(ln).find()) {
                add(findings, ReviewFinding.SEVERITY_WARNING, "security", num,
                        "疑似硬编码密钥/口令",
                        "明文凭据写进源码会随仓库泄露，且难以轮换。",
                        "改为从环境变量 / 配置中心 / 密钥管理服务读取。");
            }
        }

        // 跨行空 catch 块（单行空块也被上面正则覆盖）
        if (EMPTY_CATCH.matcher(code).find()) {
            add(findings, ReviewFinding.SEVERITY_WARNING, "bugs", null,
                    "空的 catch 块",
                    "吞掉异常且不处理，会掩盖真实错误，导致问题难以排查。",
                    "至少记录日志；若确属可忽略，加注释说明原因。");
        }

        int errors = 0, warns = 0, infos = 0;
        for (ReviewFinding f : findings) {
            if (ReviewFinding.SEVERITY_ERROR.equals(f.getSeverity())) {
                errors++;
            } else if (ReviewFinding.SEVERITY_WARNING.equals(f.getSeverity())) {
                warns++;
            } else {
                infos++;
            }
        }

        int score = 100 - errors * 15 - warns * 8 - infos * 2;
        if (score < 0) {
            score = 0;
        } else if (score > 100) {
            score = 100;
        }

        String summary;
        if (findings.isEmpty()) {
            summary = "本地规则评审完成：未发现明显的静态问题（仅做了基础检查，建议结合人工与大模型评审）。";
        } else {
            summary = String.format(
                    "本地规则评审完成：发现 %d 个错误级、%d 个警告级、%d 个提示级问题。", errors, warns, infos);
        }
        return new ReviewResult(summary, score, findings);
    }

    private void add(List<ReviewFinding> list, String severity, String category, Integer line,
                     String title, String detail, String suggestion) {
        ReviewFinding f = new ReviewFinding();
        f.setSeverity(severity);
        f.setCategory(category);
        f.setLine(line);
        f.setTitle(title);
        f.setDetail(detail);
        f.setSuggestion(suggestion);
        list.add(f);
    }
}
