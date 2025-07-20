package dev.aimusic.backend.subscription.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionModel, Long> {
    Optional<SubscriptionModel> findByUserId(Long userId);

    Optional<SubscriptionModel> findByStripeCustomerId(String stripeCustomerId);

    Optional<SubscriptionModel> findByStripeSubscriptionId(String stripeSubscriptionId);
}
