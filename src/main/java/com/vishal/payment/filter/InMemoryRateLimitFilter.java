package com.vishal.payment.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class InMemoryRateLimitFilter implements Filter {
    private final int limitPerMinute;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public InMemoryRateLimitFilter(@Value("${payment.gateway.rate-limit-per-minute:120}") int limitPerMinute) {
        this.limitPerMinute = limitPerMinute;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (!req.getRequestURI().startsWith("/api/v1/payments")) {
            chain.doFilter(request, response);
            return;
        }
        String ip = req.getHeader("X-Forwarded-For") != null ? req.getHeader("X-Forwarded-For").split(",")[0].trim() : request.getRemoteAddr();
        long minute = Instant.now().getEpochSecond() / 60;
        Window window = windows.compute(ip, (k, old) -> old == null || old.minute != minute ? new Window(minute) : old);
        if (window.count.incrementAndGet() > limitPerMinute) {
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\":\"rate limit exceeded\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private static class Window {
        final long minute;
        final AtomicInteger count = new AtomicInteger(0);
        Window(long minute) { this.minute = minute; }
    }
}
