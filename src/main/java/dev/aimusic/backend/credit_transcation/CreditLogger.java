package dev.aimusic.backend.credit_transcation;

import dev.aimusic.backend.credit_transcation.dao.TriggerSource;
import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class CreditLogger {

    public void logCreditConsumption(Long userId, Integer amount, TriggerSource triggerSource,
                                     String taskId, Integer permanentAfter, Integer renewableAfter) {
        log.info("Credit consumed - UserId: {}, Amount: {}, Source: {}, Task: {}, PermanentAfter: {}, RenewableAfter: {}",
                userId, amount, triggerSource, taskId, permanentAfter, renewableAfter);
    }

    public void logCreditReset(Long userId, PlanType planType, Integer newAmount, LocalDateTime nextResetTime) {
        log.info("Credit reset - UserId: {}, Plan: {}, NewAmount: {}, NextReset: {}",
                userId, planType.getName(), newAmount, nextResetTime);
    }

    public void logSubscriptionChange(Long userId, PlanType oldPlan, PlanType newPlan, String action) {
        log.info("Subscription changed - UserId: {}, From: {}, To: {}, Action: {}",
                userId, oldPlan.getName(), newPlan.getName(), action);
    }
}
