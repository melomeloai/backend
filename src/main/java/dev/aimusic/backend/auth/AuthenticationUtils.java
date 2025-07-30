package dev.aimusic.backend.auth;

import dev.aimusic.backend.user.dao.UserModel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;

@Slf4j
@UtilityClass
public class AuthenticationUtils {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 从HTTP请求头中提取JWT token
     *
     * @param request HTTP请求
     * @return JWT token，如果未找到则返回null
     */
    public static String extractTokenFromRequest(HttpServletRequest request) {
        var authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 从Authentication中获取用户ID
     */
    public static Long getUserId(Authentication authentication) {
        if (authentication instanceof ClerkAuthentication clerkAuth) {
            return clerkAuth.getUserId();
        }
        throw new IllegalArgumentException("Invalid authentication type");
    }

    /**
     * 从Authentication中获取用户对象
     */
    public static UserModel getUser(Authentication authentication) {
        if (authentication instanceof ClerkAuthentication clerkAuth) {
            return clerkAuth.getUser();
        }
        throw new IllegalArgumentException("Invalid authentication type");
    }
}
