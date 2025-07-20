package dev.aimusic.backend.subscription.dao;

import dev.aimusic.backend.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionDao {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionModel findByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Subscription not found for user: " + userId));
    }

    public SubscriptionModel findByStripeCustomerId(String stripeCustomerId) {
        return subscriptionRepository.findByStripeCustomerId(stripeCustomerId)
                .orElseThrow(() -> new NotFoundException("Subscription not found " +
                        "for Stripe customer ID: " + stripeCustomerId));
    }

    public SubscriptionModel save(SubscriptionModel subscription) {
        return subscriptionRepository.save(subscription);
    }

    public void createDefaultSubscription(Long userId, String stripeCustomerId) {
        log.info("Creating default FREE subscription for user: {}", userId);
        save(SubscriptionModel.builder()
                .userId(userId)
                .stripeCustomerId(stripeCustomerId)
                .planType(PlanType.FREE)
                .status("ACTIVE")
                .cancelAtPeriodEnd(false)
                .build());
    }
}