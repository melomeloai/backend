package dev.aimusic.backend.subscription.dto;

import dev.aimusic.backend.common.dto.AbstractResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Checkout Session响应DTO
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class CheckoutSessionResponse extends AbstractResponse {
    private String checkoutUrl;
}
