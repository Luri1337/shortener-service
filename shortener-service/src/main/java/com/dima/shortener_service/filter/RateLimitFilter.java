package com.dima.shortener_service.filter;

import com.dima.shortener_service.service.RateLimitService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RateLimitFilter implements Filter {
    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ip = httpRequest.getRemoteAddr();

        try {
            if (rateLimitService.isRateLimited(ip)) {
                httpResponse.setStatus(429);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("Rate limit exceeded. Please try again later.");
                return;
            }

        }catch (Exception e){
            log.warn("Redis is unavailable, request rate limiting is disabled: {}", e.getMessage());
        }


        chain.doFilter(request, response);
    }
}
