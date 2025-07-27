package dev.aimusic.backend.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestStatus {
    private String requestId;
}