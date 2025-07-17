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
import java.util.Objects;

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
        if (Objects.isNull(subscription)) {
            return null; // No subscription found for the user
        }
        // Refresh credits lazily
        return lazyRefreshCredits(subscription);
    }

    public void updatePlan(Long userId, PlanType newPlan) {
        // Get the current subscription for the user
        var subscription = subscriptionDao.findByUserId(userId);
        if (Objects.isNull(subscription)) {
            throw new IllegalArgumentException("No subscription found for user with ID: " + userId);
        }

        // Update the subscription plan
        subscription.setCurrentPlan(newPlan);
        // Reset credits based on the new plan
        subscription.setCurrentCredit(CreditUtils.getInitialCredits(newPlan));
        subscription.setLastResetAt(OffsetDateTime.now());
        subscription.setNextResetAt(CreditUtils.getNextResetAt(newPlan));

        // Save the subscription
        subscriptionDao.save(subscription);
    }

    public String createStripeSessionUrl(Long userId, PlanType planType) {
        // Get the subscription for the user
        var subscription = subscriptionDao.findByUserId(userId);
        if (Objects.isNull(subscription)) {
            throw new IllegalArgumentException("No subscription found for user with ID: " + userId);
        }

        // Check user current plan
        var currentPlan = subscription.getCurrentPlan();

        if (Objects.equals(currentPlan, planType)) {
            // If the user is already on the requested plan, no action needed
            throw new IllegalArgumentException("User is already on the requested plan: " + planType);
        }

        if (Objects.equals(currentPlan, PlanType.FREE)) {
            // If the user is on a free plan and trying to upgrade,
            // create a checkout session for the new plan
            return stripeService.createSubscriptionCheckoutSession(
                    subscription.getStripeCustomerId(),
                    planType,
                    "http://localhost:5173",
                    "http://localhost:5173?error=true"
            );
        }

        // If the user is on a paid plan and trying to change plans,
        // create a billing portal session for managing subscriptions
        return stripeService.createSubscriptionPortalSession(
                subscription.getStripeCustomerId(),
                "http://localhost:5137"
        );
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
