package dev.aimusic.backend.controller;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.auth.AuthenticationUtils;
import dev.aimusic.backend.subscription.SubscriptionService;
import dev.aimusic.backend.subscription.dao.PlanType;
import dev.aimusic.backend.subscription.dto.CheckoutSessionResponse;
import dev.aimusic.backend.subscription.dto.CustomerPortalResponse;
import dev.aimusic.backend.subscription.dto.SubscriptionInfoResponse;
import dev.aimusic.backend.subscription.dto.UpgradeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.aimusic.backend.common.Constants.API_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = API_PATH + "/subscriptions", produces = {APPLICATION_JSON_VALUE})
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 获取用户订阅信息
     * GET /api/subscriptions
     */
    @GetMapping
    public ResponseEntity<SubscriptionInfoResponse> getSubscriptionInfo(Authentication auth) {
        var userId = AuthenticationUtils.getUserId(auth);
        log.info("Getting subscription info for user: {}", userId);

        var response = subscriptionService.getUserSubscriptionInfo(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 创建升级到Pro的checkout session
     * POST /api/subscriptions/upgrade/pro
     */
    @PostMapping("/upgrade/pro")
    public ResponseEntity<CheckoutSessionResponse> createProCheckoutSession(
            Authentication auth,
            @RequestBody UpgradeRequest request) {

        var userId = AuthenticationUtils.getUserId(auth);
        log.info("Creating Pro checkout session for user: {}, billingCycle: {}",
                userId, request.getBillingCycle());

        validateBillingCycle(request.getBillingCycle());
        var checkoutUrl = subscriptionService.createCheckoutSession(
                userId, PlanType.PRO, request.getBillingCycle());
        var response = CheckoutSessionResponse.builder()
                .checkoutUrl(checkoutUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 创建升级到Premium的checkout session
     * POST /api/subscriptions/upgrade/premium
     */
    @PostMapping("/upgrade/premium")
    public ResponseEntity<CheckoutSessionResponse> createPremiumCheckoutSession(
            Authentication auth,
            @RequestBody UpgradeRequest request) {

        var userId = AuthenticationUtils.getUserId(auth);
        log.info("Creating Premium checkout session for user: {}, billingCycle: {}",
                userId, request.getBillingCycle());

        validateBillingCycle(request.getBillingCycle());
        var checkoutUrl = subscriptionService.createCheckoutSession(
                userId, PlanType.PREMIUM, request.getBillingCycle());
        var response = CheckoutSessionResponse.builder()
                .checkoutUrl(checkoutUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 获取客户管理portal URL
     * POST /api/subscriptions/portal
     */
    @PostMapping("/portal")
    public ResponseEntity<CustomerPortalResponse> createCustomerPortalSession(
            Authentication auth) {
        var userId = AuthenticationUtils.getUserId(auth);
        log.info("Creating customer portal session for user: {}", userId);

        var portalUrl = subscriptionService.createCustomerPortalSession(userId);
        var response = CustomerPortalResponse.builder()
                .portalUrl(portalUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    // ===== 辅助方法（使用@VisibleForTesting便于单元测试） =====

    @VisibleForTesting
    void validateBillingCycle(String billingCycle) {
        if (!"monthly".equals(billingCycle) && !"yearly".equals(billingCycle)) {
            throw new IllegalArgumentException("Invalid billing cycle: " + billingCycle);
        }
    }
}
