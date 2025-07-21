package dev.aimusic.backend.subscription.dto;

import dev.aimusic.backend.subscription.dao.BillingCycle;
import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 升级请求DTO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeRequest {
    private PlanType planType; // PRO, PREMIUM
    private BillingCycle billingCycle; // "monthly" | "yearly"
}
