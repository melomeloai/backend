package dev.aimusic.backend.subscription.transformer;

import dev.aimusic.backend.subscription.dao.SubscriptionModel;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SubscriptionTransformer {

    public static SubscriptionResponse toSubscriptionResponse(SubscriptionModel subscription) {
        return SubscriptionResponse.builder()
                .userId(subscription.getUserId())
                .currentPlan(subscription.getCurrentPlan())
                .currentCredit(subscription.getCurrentCredit())
                .nextResetAt(subscription.getNextResetAt())
                .build();
    }
}
