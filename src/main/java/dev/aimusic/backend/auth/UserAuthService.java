package dev.aimusic.backend.auth;

import dev.aimusic.backend.clients.clerk.ClerkService;
import dev.aimusic.backend.user.UserCreationService;
import dev.aimusic.backend.user.dao.UserModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthService {

    private final ClerkService clerkService;
    private final UserCreationService userCreationService;

    /**
     * 验证JWT token并返回用户信息
     */
    public UserModel validateTokenAndGetUser(String token) {
        try {
            var tokenModel = clerkService.authenticate(token);

            var clerkId = tokenModel.getSub();
            var email = tokenModel.getEmail();
            var name = tokenModel.getName();

            if (StringUtils.isBlank(clerkId)) {
                throw new IllegalArgumentException("Invalid token: missing subject");
            }

            if (StringUtils.isBlank(email)) {
                throw new IllegalArgumentException("Invalid token: missing email");
            }
            return userCreationService.initializeNewUser(clerkId, email, name);

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }
}
