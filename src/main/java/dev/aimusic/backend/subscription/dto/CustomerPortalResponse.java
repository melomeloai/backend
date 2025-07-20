package dev.aimusic.backend.subscription.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Customer Portal响应DTO
 */
@Builder
@Data
public class CustomerPortalResponse {
    private String portalUrl;
}
