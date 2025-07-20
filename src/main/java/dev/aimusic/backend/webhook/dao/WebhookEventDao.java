package dev.aimusic.backend.webhook.dao;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookEventDao {

    private final WebhookEventRepository webhookEventRepository;

    public Optional<WebhookEventModel> findByStripeEventId(String stripeEventId) {
        return webhookEventRepository.findByStripeEventId(stripeEventId);
    }

    public List<WebhookEventModel> findUnprocessedEvents() {
        return webhookEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
    }

    public WebhookEventModel save(WebhookEventModel event) {
        return webhookEventRepository.save(event);
    }

    public boolean isEventProcessed(String stripeEventId) {
        return findByStripeEventId(stripeEventId)
                .map(WebhookEventModel::getProcessed)
                .orElse(false);
    }
}

