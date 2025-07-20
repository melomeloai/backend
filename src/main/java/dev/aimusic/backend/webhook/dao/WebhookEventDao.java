package dev.aimusic.backend.webhook.dao;


import dev.aimusic.backend.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookEventDao {

    private final WebhookEventRepository webhookEventRepository;

    public WebhookEventModel findByStripeEventId(String stripeEventId) {
        return webhookEventRepository.findById(stripeEventId)
                .orElseThrow(() -> new NotFoundException(
                        "Webhook event not found for Stripe event ID: " + stripeEventId));
    }

    public WebhookEventModel save(WebhookEventModel event) {
        return webhookEventRepository.save(event);
    }

    public boolean isEventProcessed(String stripeEventId) {
        return webhookEventRepository.findById(stripeEventId)
                .map(WebhookEventModel::getProcessed)
                .orElse(false);
    }
}

