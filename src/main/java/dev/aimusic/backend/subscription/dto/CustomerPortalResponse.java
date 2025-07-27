package dev.aimusic.backend.subscription.dto;

import dev.aimusic.backend.common.AbstractResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Customer Portal响应DTO
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class CustomerPortalResponse extends AbstractResponse {
    private String portalUrl;
}
