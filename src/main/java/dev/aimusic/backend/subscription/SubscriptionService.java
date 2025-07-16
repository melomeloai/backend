package dev.aimusic.backend.subscription;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.clients.stripe.StripeService;
import dev.aimusic.backend.subscription.dao.PlanType;
import dev.aimusic.backend.subscription.dao.SubscriptionDao;
import dev.aimusic.backend.subscription.dao.SubscriptionModel;
import dev.aimusic.backend.user.dao.UserModel;
import dev.aimusic.backend.utils.CreditUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionDao subscriptionDao;
    private final StripeService stripeService;

    public void initSubscription(UserModel user) {
        // Create a Stripe customer for the user
        var stripeCustomerId = stripeService.createCustomer(user.getEmail());

        // Initialize the subscription for the user
        var initialPlan = PlanType.FREE; // Default to free plan
        var subscriptionModel = SubscriptionModel.builder()
                .userId(user.getId())
                .stripeCustomerId(stripeCustomerId)
                .currentPlan(initialPlan) // Set initial plan
                .currentCredit(CreditUtils.getInitialCredits(initialPlan))
                .lastResetAt(OffsetDateTime.now())
                .nextResetAt(CreditUtils.getNextResetAt(initialPlan))
                .build();

        // Save the subscription
        subscriptionDao.save(subscriptionModel);
    }

    public SubscriptionModel getSubscriptionByUserId(Long userId) {
        var subscription = subscriptionDao.findByUserId(userId);
        if (subscription == null) {
            return null; // No subscription found for the user
        }
        // Refresh credits lazily
        return lazyRefreshCredits(subscription);
    }

    @VisibleForTesting
    SubscriptionModel lazyRefreshCredits(SubscriptionModel subscription) {
        // This method is used to refresh the credits of a subscription lazily
        if (OffsetDateTime.now().isAfter(subscription.getNextResetAt())) {
            // Reset credits based on the current plan
            var newCredits = CreditUtils.getInitialCredits(subscription.getCurrentPlan());
            subscription.setCurrentCredit(newCredits);
            subscription.setLastResetAt(OffsetDateTime.now());
            subscription.setNextResetAt(CreditUtils.getNextResetAt(subscription.getCurrentPlan()));
            return subscriptionDao.save(subscription);
        }
        return subscription;
    }
}
