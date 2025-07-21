package dev.aimusic.backend.subscription.dto;

import dev.aimusic.backend.subscription.dao.BillingCycle;
import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.Builder;
import lombok.Data;

/**
 * 升级请求DTO
 */
@Builder
@Data
public class UpgradeRequest {
    private PlanType planType; // PRO, PREMIUM
    private BillingCycle billingCycle; // "monthly" | "yearly"
}
