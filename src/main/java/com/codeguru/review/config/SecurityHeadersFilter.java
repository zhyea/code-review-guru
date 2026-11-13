package com.codeguru.review.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 统一下发安全响应头（防御纵深，低成本高收益）。CSP 现在<strong>零豁免</strong>：
 * 无 {@code 'unsafe-inline'}、无 {@code 'unsafe-eval'}、无外部 CDN 白名单。
 *
 * <p>代价是前端必须满足两条约束（改动前端前必读）：</p>
 * <ol>
 *   <li>资源全部本地同源：Tailwind 走构建产物 {@code static/css/tailwind.css}，脚本只有
 *       {@code static/js/app.js}（原生 JS，零依赖）。</li>
 *   <li>不得写内联事件属性（{@code onclick} / {@code onload}…）与内联 {@code <style>}，
 *       交互一律走 {@code data-*} 属性 + {@code app.js} 的 {@code addEventListener}。</li>
 *   <li>不得引入依赖 {@code eval} / {@code new Function} 的库（会被 CSP 静默拦死）。</li>
 * </ol>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter implements Filter {

    private static final String CSP =
            "default-src 'self'; "
            + "script-src 'self'; "
            + "style-src 'self'; "
            + "img-src 'self' data:; "
            + "font-src 'self' data:; "
            + "frame-ancestors 'none'; "
            + "base-uri 'self'; "
            + "form-action 'self'";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse http) {
            http.setHeader("X-Content-Type-Options", "nosniff");
            http.setHeader("X-Frame-Options", "DENY");
            http.setHeader("Referrer-Policy", "same-origin");
            http.setHeader("Content-Security-Policy", CSP);
            http.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            http.setHeader("Cross-Origin-Opener-Policy", "same-origin");
            http.setHeader("Cross-Origin-Resource-Policy", "same-origin");
            http.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        }
        chain.doFilter(request, response);
    }
}
