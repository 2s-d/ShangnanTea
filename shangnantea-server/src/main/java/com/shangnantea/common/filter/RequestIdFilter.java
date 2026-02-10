package com.shangnantea.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求ID过滤器。
 *
 * <p>用于在每次请求链路中注入 requestId，解决“APIFox 看到失败，但需要复制粘贴错误到后端日志里查”的问题：
 * <ul>
 *     <li>优先读取请求头 {@code X-Request-Id}（便于客户端自定义与重放）</li>
 *     <li>若不存在则自动生成</li>
 *     <li>写入 MDC，配合日志 pattern 在控制台/文件日志中输出</li>
 *     <li>回写到响应头 {@code X-Request-Id}，便于在 APIFox 里直接看到并据此筛日志</li>
 * </ul>
 * </p>
 */
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_KEY_REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put(MDC_KEY_REQUEST_ID, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY_REQUEST_ID);
        }
    }
}

