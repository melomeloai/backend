package dev.aimusic.backend.subscription.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Checkout Session响应DTO
 */
@Builder
@Data
public class CheckoutSessionResponse {
    private String checkoutUrl;
}
