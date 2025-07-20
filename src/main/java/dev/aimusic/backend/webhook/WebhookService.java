package dev.aimusic.backend.webhook;

import com.google.common.annotations.VisibleForTesting;
import com.stripe.model.Event;
import dev.aimusic.backend.subscription.SubscriptionService;
import dev.aimusic.backend.webhook.dao.WebhookEventDao;
import dev.aimusic.backend.webhook.dao.WebhookEventModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    // 定义需要处理和存储的事件类型
    private static final Set<String> HANDLED_EVENT_TYPES = Set.of(
            "customer.subscription.created",
            "customer.subscription.updated",
            "customer.subscription.deleted",
            "invoice.payment_succeeded",
            "invoice.payment_failed"
    );
    private final WebhookEventDao webhookEventDao;
    private final SubscriptionService subscriptionService;

    /**
     * 处理Stripe webhook事件
     */
    public void processStripeEvent(Event event) {
        // 1. 检查是否是需要处理的事件类型
        if (!HANDLED_EVENT_TYPES.contains(event.getType())) {
            log.debug("Event type {} not handled, skipping", event.getType());
            return;
        }

        // 2. 检查事件是否已处理（幂等性）
        if (webhookEventDao.isEventProcessed(event.getId())) {
            log.info("Event {} already processed, skipping", event.getId());
            return;
        }

        // 3. 记录事件（只记录需要处理的事件）
        var webhookEvent = createWebhookEventRecord(event);
        webhookEventDao.save(webhookEvent);

        try {
            // 4. 根据事件类型分发处理
            processEventByType(event);

            // 5. 标记为已处理
            markEventAsProcessed(webhookEvent);

        } catch (Exception e) {
            // 6. 处理失败时记录错误信息
            handleEventProcessingFailure(webhookEvent, e);
            throw e;
        }
    }

    // ===== 辅助方法（使用@VisibleForTesting便于单元测试） =====

    @VisibleForTesting
    WebhookEventModel createWebhookEventRecord(Event event) {
        return WebhookEventModel.builder()
                .stripeEventId(event.getId())
                .eventType(event.getType())
                .eventData(event.toJson()) // 注意：这里可能需要根据数据库字段类型调整
                .processed(false)
                .retryCount(0)
                .build();
    }

    @VisibleForTesting
    void processEventByType(Event event) {
        switch (event.getType()) {
            case "customer.subscription.created",
                 "customer.subscription.updated" -> {
                handleSubscriptionChange(event);
            }
            case "customer.subscription.deleted" -> {
                handleSubscriptionCancellation(event);
            }
            case "invoice.payment_succeeded" -> {
                handlePaymentSuccess(event);
            }
            case "invoice.payment_failed" -> {
                handlePaymentFailure(event);
            }
            default -> log.debug("Unhandled event type: {}", event.getType());
        }
    }

    @VisibleForTesting
    com.stripe.model.Subscription parseStripeSubscription(Event event) {
        var dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isEmpty()) {
            throw new IllegalArgumentException("Failed to parse Stripe subscription from event: " + event.getId());
        }
        return (com.stripe.model.Subscription) dataObjectDeserializer.getObject().get();
    }

    @VisibleForTesting
    void handleSubscriptionChange(Event event) {
        log.info("Handling subscription change for event: {}", event.getId());
        try {
            var stripeSubscription = parseStripeSubscription(event);
            subscriptionService.handleSubscriptionChange(stripeSubscription);
        } catch (Exception e) {
            log.error("Failed to process subscription change for event {}: {}", event.getId(), e.getMessage());
            throw new RuntimeException("Failed to process subscription change", e);
        }
    }

    @VisibleForTesting
    void handleSubscriptionCancellation(Event event) {
        log.info("Handling subscription cancellation for event: {}", event.getId());
        try {
            var stripeSubscription = parseStripeSubscription(event);
            subscriptionService.handleSubscriptionCancellation(stripeSubscription);
        } catch (Exception e) {
            log.error("Failed to process subscription cancellation for event {}: {}", event.getId(), e.getMessage());
            throw new RuntimeException("Failed to process subscription cancellation", e);
        }
    }

    @VisibleForTesting
    void handlePaymentSuccess(Event event) {
        log.info("Handling payment success for event: {}", event.getId());
        // 处理支付成功逻辑
    }

    @VisibleForTesting
    void handlePaymentFailure(Event event) {
        log.info("Handling payment failure for event: {}", event.getId());
        // 处理支付失败逻辑
    }

    @VisibleForTesting
    void markEventAsProcessed(WebhookEventModel event) {
        event.setProcessed(true);
        event.setProcessedAt(LocalDateTime.now());
        webhookEventDao.save(event);
    }

    @VisibleForTesting
    void handleEventProcessingFailure(WebhookEventModel event, Exception e) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setErrorMessage(e.getMessage());
        webhookEventDao.save(event);

        log.error("Webhook event processing failed for event {}: {}", event.getStripeEventId(), e.getMessage());
    }
}