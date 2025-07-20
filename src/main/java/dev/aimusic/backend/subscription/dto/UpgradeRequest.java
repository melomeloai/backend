package dev.aimusic.backend.subscription.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 升级请求DTO
 */
@Builder
@Data
public class UpgradeRequest {
    private String billingCycle; // "monthly" | "yearly"
}
