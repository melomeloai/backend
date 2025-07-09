package dev.aimusic.backend.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AuthUser implements OAuth2User {

    @Getter
    private final String provider;
    @Getter
    private final String externalId;
    @Getter
    private final String email;
    @Getter
    private final String name;
    @Getter
    private final String avatarUrl;
    private final Map<String, Object> attributes;

    public AuthUser(String provider, Map<String, Object> attributes) {
        this.provider = provider;
        this.attributes = attributes;
        this.externalId = extractExternalId(provider, attributes);
        this.email = (String) attributes.get("email");
        this.name = (String) attributes.get("name");
        this.avatarUrl = extractAvatarUrl(provider, attributes);
    }

    private String extractExternalId(String provider, Map<String, Object> attrs) {
        return switch (provider) {
            case "google", "apple" -> (String) attrs.get("sub");
            case "github" -> String.valueOf(attrs.get("id"));
            default -> throw new RuntimeException("Unsupported provider: " + provider);
        };
    }

    private String extractAvatarUrl(String provider, Map<String, Object> attrs) {
        return switch (provider) {
            case "google" -> (String) attrs.get("picture");
            case "github" -> (String) attrs.get("avatar_url");
            default -> null;
        };
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
