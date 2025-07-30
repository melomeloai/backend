package dev.aimusic.backend.auth;

import dev.aimusic.backend.clients.clerk.ClerkService;
import dev.aimusic.backend.common.exceptions.AuthenticationException;
import dev.aimusic.backend.user.dao.UserDao;
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

    private final ClerkService clerkService;
    private final UserDao userDao;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            var token = AuthenticationUtils.extractTokenFromRequest(request);

            if (token != null) {
                var tokenModel = clerkService.authenticate(token);
                var clerkId = tokenModel.getSub();

                if (StringUtils.isBlank(clerkId)) {
                    throw new AuthenticationException("Invalid token: missing subject");
                }

                var user = userDao.findByClerkId(clerkId);
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

}
