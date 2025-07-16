package dev.aimusic.backend.subscription.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionDao {
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionModel findByUserId(Long userId) {
        return subscriptionRepository.findById(userId).orElse(null);
    }

    public SubscriptionModel save(SubscriptionModel subscription) {
        return subscriptionRepository.save(subscription);
    }
}
