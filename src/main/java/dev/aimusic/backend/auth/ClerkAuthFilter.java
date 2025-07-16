package dev.aimusic.backend.auth;

import dev.aimusic.backend.clients.clerk.ClerkService;
import dev.aimusic.backend.user.UserService;
import dev.aimusic.backend.utils.HeaderUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClerkAuthFilter extends OncePerRequestFilter {


    private final ClerkService clerkService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        var token = HeaderUtils.getBearerTokenFromRequest(request);

        try {
            // 验证并解析 Clerk Token
            var session = clerkService.authenticate(token);

            // 查找或创建用户
            var user = userService.findOrCreateUser(
                    session.getSub(),
                    session.getEmail(),
                    session.getName()
            );

            // 设置当前用户上下文
            UserContext.set(user);

        } catch (RuntimeException e) {
            log.warn("Clerk token validation failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}

