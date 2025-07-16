package dev.aimusic.backend.utils;

import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;

@UtilityClass
public class CreditUtils {

    public static Integer getInitialCredits(PlanType planType) {
        return switch (planType) {
            case FREE -> 10;
            case PRO -> 500;
            case PREMIUM -> 2000;
        };
    }

    public static OffsetDateTime getNextResetAt(PlanType planType) {
        return switch (planType) {
            case FREE -> OffsetDateTime.now().plusDays(1);
            case PRO, PREMIUM -> OffsetDateTime.now().plusDays(30);
        };
    }
}
