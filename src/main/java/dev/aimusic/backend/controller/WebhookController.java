package dev.aimusic.backend.controller;

import dev.aimusic.backend.clients.stripe.StripeService;
import dev.aimusic.backend.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.aimusic.backend.common.Constants.API_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = API_PATH + "/webhooks", produces = {APPLICATION_JSON_VALUE})
public class WebhookController {

    private final WebhookService webhookService;
    private final StripeService stripeService;

    /**
     * 处理Stripe webhook事件
     * POST /api/webhooks/stripe
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        log.info("Received Stripe webhook");

        try {
            // 1. 验证webhook签名并解析事件
            var stripeEvent = stripeService.verifyAndParseWebhook(payload, signature);

            log.info("Processing webhook event: {} of type: {}", stripeEvent.getId(), stripeEvent.getType());

            // 2. 处理事件
            webhookService.processStripeEvent(stripeEvent);

            return ResponseEntity.ok("success");

        } catch (IllegalArgumentException e) {
            log.warn("Invalid webhook signature");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Failed to process webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Processing failed");
        }
    }
}
