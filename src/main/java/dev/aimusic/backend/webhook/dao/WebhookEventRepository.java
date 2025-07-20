package dev.aimusic.backend.webhook.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEventModel, Long> {
    Optional<WebhookEventModel> findByStripeEventId(String stripeEventId);

    List<WebhookEventModel> findByProcessedFalseOrderByCreatedAtAsc();
}
