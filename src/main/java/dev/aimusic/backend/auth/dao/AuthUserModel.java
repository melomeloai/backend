package dev.aimusic.backend.auth.dao;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthUserModel {
    private final String provider;
    private final String externalId;
    private final String email;
    private final String name;
    private final String avatarUrl;
}
