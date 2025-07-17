package dev.aimusic.backend.subscription.transformer;

import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.Builder;

@Builder
public record ChangeSubscriptionRequest(
        PlanType planType
) {
}
