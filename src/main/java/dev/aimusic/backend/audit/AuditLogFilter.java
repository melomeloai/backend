package dev.aimusic.backend.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.aimusic.backend.common.Constants.X_REQUEST_ID;
import static dev.aimusic.backend.common.Constants.X_USER_ID;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.USER_AGENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Generate a unique request ID for tracking
        response.setHeader(X_REQUEST_ID, UUID.randomUUID().toString());

        try {
            chain.doFilter(request, response);
        } finally {
            logAudit(request, response);
        }
    }

    private void logAudit(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("method", request.getMethod());
            auditData.put("requestUrl", request.getRequestURL());
            auditData.put("remoteAddress", request.getRemoteAddr());
            auditData.put("userAgent", request.getHeader(USER_AGENT));
            auditData.put("timestamp", LocalDateTime.now());
            auditData.put("requestId", response.getHeader(X_REQUEST_ID));
            auditData.put("userId", response.getHeader(X_USER_ID));
            auditData.put("statusCode", response.getStatus());
            auditData.put("contentType", response.getContentType());
            auditData.put("contentLength", response.getHeader(CONTENT_LENGTH));

            String jsonLog = objectMapper.writeValueAsString(auditData);
            log.info("{}", jsonLog);
        } catch (Exception e) {
            log.error("Failed to log audit", e);
        }
    }

}