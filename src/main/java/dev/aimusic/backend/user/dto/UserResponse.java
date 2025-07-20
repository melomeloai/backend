package dev.aimusic.backend.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;
}
