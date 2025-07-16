package dev.aimusic.backend.clients.clerk;

import com.clerk.backend_api.helpers.security.models.VerifyTokenOptions;
import com.clerk.backend_api.helpers.security.token_verifiers.impl.JwtSessionTokenVerifier;
import dev.aimusic.backend.config.ClerkProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClerkService {
    static final String CLERK_CLAIM_NAME = "name";
    static final String CLERK_CLAIM_EMAIL = "email";

    private final ClerkProperties clerkProperties;

    public ClerkService(ClerkProperties clerkProperties) {
        this.clerkProperties = clerkProperties;
    }

    public ClerkSessionTokenModel authenticate(String token) {
        try {
            var verifyRespose = JwtSessionTokenVerifier.verify(
                    token,
                    VerifyTokenOptions
                            .secretKey(clerkProperties.getApiKey())
                            .build());

            var claims = verifyRespose.payload();

            if (!StringUtils.equals(claims.getIssuer(),
                    clerkProperties.getJwtIssuer())) {
                throw new RuntimeException("Invalid token issuer");
            }

            return ClerkSessionTokenModel.builder()
                    .sub(claims.getSubject())
                    .email(claims.get(CLERK_CLAIM_EMAIL, String.class))
                    .name(claims.get(CLERK_CLAIM_NAME, String.class))
                    .issuer(claims.getIssuer())
                    .issuedAt(claims.getIssuedAt())
                    .expiresAt(claims.getExpiration())
                    .audience(claims.getAudience())
                    .build();

        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid Clerk token", e);
        }
    }
}
