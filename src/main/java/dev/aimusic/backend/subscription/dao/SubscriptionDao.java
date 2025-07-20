package dev.aimusic.backend.subscription.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionDao {

    private final SubscriptionRepository subscriptionRepository;

    public Optional<SubscriptionModel> findByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    public Optional<SubscriptionModel> findByStripeCustomerId(String stripeCustomerId) {
        return subscriptionRepository.findByStripeCustomerId(stripeCustomerId);
    }

    public Optional<SubscriptionModel> findByStripeSubscriptionId(String stripeSubscriptionId) {
        return subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);
    }

    public SubscriptionModel save(SubscriptionModel subscription) {
        return subscriptionRepository.save(subscription);
    }

    public SubscriptionModel createDefaultSubscription(Long userId, String stripeCustomerId) {
        var subscription = SubscriptionModel.builder()
                .userId(userId)
                .stripeCustomerId(stripeCustomerId)
                .planType(PlanType.FREE)
                .status("ACTIVE")
                .cancelAtPeriodEnd(false)
                .nextResetTime(LocalDateTime.now().plusDays(1)) // FREE plan每日重置
                .build();

        log.info("Creating default FREE subscription for user: {}", userId);
        return save(subscription);
    }
}