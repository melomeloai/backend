package dev.aimusic.backend.subscription.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionModel, Long> {
    Optional<SubscriptionModel> findByStripeCustomerId(String stripeCustomerId);
}
