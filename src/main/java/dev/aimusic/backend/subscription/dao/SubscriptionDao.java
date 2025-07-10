package dev.aimusic.backend.subscription.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionDao {

    private final SubscriptionRepository subscriptionRepository;

    public List<SubscriptionModel> listSubscriptionsByUserId(String userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    public SubscriptionModel save(SubscriptionModel subscription) {
        return subscriptionRepository.save(subscription);
    }
}
