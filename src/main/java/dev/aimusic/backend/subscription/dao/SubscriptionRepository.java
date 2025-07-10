package dev.aimusic.backend.subscription.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<SubscriptionModel, String> {
    /**
     * Finds all subscriptions for a given user ID.
     *
     * @param userId the ID of the user whose subscriptions are to be found
     * @return a list of SubscriptionModel objects associated with the user
     */
    List<SubscriptionModel> findByUserId(String userId);
}