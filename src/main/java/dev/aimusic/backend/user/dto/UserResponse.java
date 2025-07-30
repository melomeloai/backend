package dev.aimusic.backend.user.dto;

import dev.aimusic.backend.common.dto.AbstractResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class UserResponse extends AbstractResponse {
    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;
}
