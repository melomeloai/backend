package dev.aimusic.backend.auth;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.auth.dao.AuthUserModel;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    static final String GOOGLE_REGISTRATION_ID = "google";
    static final String GITHUB_REGISTRATION_ID = "github";
    static final String APPLE_REGISTRATION_ID = "apple";

    public AuthUserModel getAuthUserModel(OAuth2AuthenticationToken token) {
        var provider = token.getAuthorizedClientRegistrationId();
        var attributes = token.getPrincipal().getAttributes();

        return AuthUserModel.builder()
                .provider(provider)
                .externalId(extractExternalId(provider, attributes))
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .avatarUrl(extractAvatarUrl(provider, attributes))
                .build();
    }

    @VisibleForTesting
    String extractExternalId(String provider, Map<String, Object> attrs) {
        return switch (provider) {
            case GOOGLE_REGISTRATION_ID, APPLE_REGISTRATION_ID -> (String) attrs.get("sub");
            case GITHUB_REGISTRATION_ID -> String.valueOf(attrs.get("id"));
            default -> throw new RuntimeException("Unsupported provider: " + provider);
        };
    }

    @VisibleForTesting
    String extractAvatarUrl(String provider, Map<String, Object> attrs) {
        return switch (provider) {
            case GOOGLE_REGISTRATION_ID -> (String) attrs.get("picture");
            case GITHUB_REGISTRATION_ID -> (String) attrs.get("avatar_url");
            case APPLE_REGISTRATION_ID -> null;
            default -> throw new RuntimeException("Unsupported provider: " + provider);
        };
    }
}
