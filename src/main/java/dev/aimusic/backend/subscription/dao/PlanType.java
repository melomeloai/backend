package dev.aimusic.backend.subscription.dao;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlanType {
    FREE("FREE", 10, "day"),
    PRO("PRO", 500, "month"),
    PREMIUM("PREMIUM", 2000, "month");

    private final String name;
    private final int resetAmount;
    private final String resetPeriod;

    @JsonCreator
    public static PlanType fromString(String name) {
        for (var plan : values()) {
            if (plan.getName().equalsIgnoreCase(name)) {
                return plan;
            }
        }
        throw new IllegalArgumentException("Unknown PlanType: " + name);
    }

    @JsonValue
    public String getName() {
        return name;
    }
}