package dev.aimusic.backend.subscription.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订阅信息响应DTO
 */
@Builder
@Data
public class SubscriptionInfoResponse {
    private String planType;                    // FREE, PRO, PREMIUM
    private String billingCycle;               // MONTHLY, YEARLY, null for free
    private String status;                     // ACTIVE, CANCELLED, EXPIRED
    private boolean cancelAtPeriodEnd;         // 是否在周期结束时取消
    private LocalDateTime currentPeriodStart;  // 当前周期开始时间
    private LocalDateTime currentPeriodEnd;    // 当前周期结束时间
}
