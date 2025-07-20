package dev.aimusic.backend.subscription.dao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlanType {
    FREE("FREE", 0, 10, "day"),
    PRO("PRO", 500, 500, "month"),
    PREMIUM("PREMIUM", 2000, 2000, "month");

    private final String name;
    private final int maxCredits;
    private final int resetAmount;
    private final String resetPeriod;

    public static PlanType fromString(String name) {
        for (var plan : values()) {
            if (plan.getName().equalsIgnoreCase(name)) {
                return plan;
            }
        }
        return FREE; // 默认返回FREE
    }
}