package dev.aimusic.backend.subscription.transformer;

import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record SubscriptionResponse(
        Long userId,
        PlanType currentPlan,
        Integer currentCredit,
        OffsetDateTime nextResetAt
) {
}