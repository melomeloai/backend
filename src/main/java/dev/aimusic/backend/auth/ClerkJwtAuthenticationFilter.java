package dev.aimusic.backend.auth;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.clients.clerk.ClerkService;
import dev.aimusic.backend.common.exceptions.AuthenticationException;
import dev.aimusic.backend.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static dev.aimusic.backend.common.Constants.X_USER_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClerkJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ClerkService clerkService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            var token = extractTokenFromRequest(request);

            if (token != null) {
                var tokenModel = clerkService.authenticate(token);

                var clerkId = tokenModel.getSub();
                var email = tokenModel.getEmail();
                var name = tokenModel.getName();

                if (StringUtils.isBlank(clerkId)) {
                    throw new AuthenticationException("Invalid token: missing subject");
                }

                if (StringUtils.isBlank(email)) {
                    throw new AuthenticationException("Invalid token: missing email");
                }

                var user = userService.findOrCreateUser(clerkId, email, name);
                var authentication = new ClerkAuthentication(user, token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                response.setHeader(X_USER_ID, String.valueOf(user.getId()));
            }
        } catch (Exception e) {
            log.debug("JWT authentication failed: {}", e.getMessage());
            // 不抛出异常，让请求继续，由后续的认证检查处理未认证的情况
        }

        filterChain.doFilter(request, response);
    }

    @VisibleForTesting
    String extractTokenFromRequest(HttpServletRequest request) {
        var authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
