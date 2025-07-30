package dev.aimusic.backend.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestStatus {
    private String requestId;
    private String error; // 错误代码，例如 "INVALID_REQUEST"
    private String errorMessage;
}