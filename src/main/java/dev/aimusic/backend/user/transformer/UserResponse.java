package dev.aimusic.backend.user.transformer;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record UserResponse(
        Long id,
        String email,
        String name,
        OffsetDateTime createdAt
) {
}