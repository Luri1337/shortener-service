package com.dima.shortener_service.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws ServletException, IOException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            String correlationId = httpRequest.getHeader("X-Correlation-Id") != null
                    ? httpRequest.getHeader("X-Correlation-Id")
                    : UUID.randomUUID().toString();

            MDC.put("correlationId", correlationId);
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}


