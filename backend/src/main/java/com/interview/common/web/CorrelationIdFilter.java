package com.interview.common.web;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String MDC_KEY = "correlationId";
    public static final String HEADER_PRIMARY = "X-Correlation-Id";
    public static final String HEADER_ALTERNATE = "Correlation-Id";

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false; // propagate across async as well
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String incoming = firstNonBlank(
                request.getHeader(HEADER_PRIMARY),
                request.getHeader(HEADER_ALTERNATE)
        );

        String correlationId = (incoming != null) ? incoming : UUID.randomUUID().toString();

        // Put in MDC for all logs in this thread
        MDC.put(MDC_KEY, correlationId);

        // Echo back for clients and downstream services
        response.setHeader(HEADER_PRIMARY, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Important: avoid MDC leaks in thread pools
            MDC.remove(MDC_KEY);
        }
    }

    private static String firstNonBlank(String a, String b) {
        return Optional.ofNullable(a).filter(s -> !s.isBlank()).orElseGet(() ->
                Optional.ofNullable(b).filter(s -> !s.isBlank()).orElse(null));
    }
}
