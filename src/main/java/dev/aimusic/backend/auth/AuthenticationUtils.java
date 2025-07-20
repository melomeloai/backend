package dev.aimusic.backend.auth;

import dev.aimusic.backend.user.dao.UserModel;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

@Slf4j
@UtilityClass
public class AuthenticationUtils {

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

    /**
     * 从Authentication中获取Clerk ID
     */
    public static String getClerkId(Authentication authentication) {
        if (authentication instanceof ClerkAuthentication clerkAuth) {
            return clerkAuth.getUser().getClerkId();
        }
        throw new IllegalArgumentException("Invalid authentication type");
    }
}
