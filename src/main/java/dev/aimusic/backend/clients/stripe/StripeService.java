package dev.aimusic.backend.clients.stripe;

import com.google.common.annotations.VisibleForTesting;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.billingportal.SessionCreateParams;
import dev.aimusic.backend.config.StripeProperties;
import dev.aimusic.backend.subscription.dao.BillingCycle;
import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION;

@Service
@Slf4j
public class StripeService {
    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;

    public StripeService(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
        this.stripeClient = new StripeClient(stripeProperties.getSecretKey());
    }

    /**
     * 创建Stripe customer
     */
    public String createCustomer(String email) {
        var customerParams = CustomerCreateParams.builder()
                .setEmail(email)
                .build();
        try {
            var customer = stripeClient.customers().create(customerParams);
            return customer.getId();
        } catch (Exception e) {
            // Fail the request if stripe customer creation fails
            throw new RuntimeException("Failed to create Stripe customer", e);
        }
    }

    /**
     * 创建checkout session
     */
    public String createCheckoutSession(String customerId, PlanType planType, BillingCycle billingCycle, Long userId) {
        try {
            var sessionParams = buildCheckoutSessionParams(customerId, planType, billingCycle, userId);
            var session = stripeClient.checkout().sessions().create(sessionParams);

            log.info("Created checkout session for customer {}: {}", customerId, session.getId());
            return session.getUrl();

        } catch (Exception e) {
            log.error("Failed to create checkout session for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Failed to create checkout session", e);
        }
    }

    /**
     * 创建customer portal session
     */
    public String createCustomerPortalSession(String customerId) {
        try {
            var portalParams = buildCustomerPortalParams(customerId);
            var portalSession = stripeClient.billingPortal().sessions().create(portalParams);

            log.info("Created customer portal session for customer {}: {}", customerId, portalSession.getId());
            return portalSession.getUrl();

        } catch (Exception e) {
            log.error("Failed to create customer portal session for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Failed to create customer portal session", e);
        }
    }

    /**
     * 验证webhook签名并返回解析后的Event对象
     */
    public com.stripe.model.Event verifyAndParseWebhook(String payload, String signature) {
        try {
            return Webhook.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Invalid webhook signature: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid webhook signature", e);
        } catch (Exception e) {
            log.error("Failed to parse webhook event: {}", e.getMessage());
            throw new RuntimeException("Failed to parse webhook event", e);
        }
    }

    /**
     * 根据productId获取PlanType
     */
    public PlanType getPlanTypeByProductId(String productId) {
        if (StringUtils.equals(productId, stripeProperties.getProProductId())) {
            return PlanType.PRO;
        } else if (StringUtils.equals(productId, stripeProperties.getPremiumProductId())) {
            return PlanType.PREMIUM;
        } else {
            throw new IllegalArgumentException("Unknown product ID: " + productId);
        }
    }

    // ===== 辅助方法（使用@VisibleForTesting便于单元测试） =====

    @VisibleForTesting
    com.stripe.param.checkout.SessionCreateParams buildCheckoutSessionParams(
            String customerId, PlanType planType, BillingCycle billingCycle, Long userId) {
        var priceId = getPriceId(planType, billingCycle);

        return com.stripe.param.checkout.SessionCreateParams.builder()
                .setMode(SUBSCRIPTION)
                .setCustomer(customerId)
                .addLineItem(com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build())
                .setSuccessUrl(stripeProperties.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(stripeProperties.getCancelUrl())
                .setAllowPromotionCodes(true)
                .setSubscriptionData(com.stripe.param.checkout.SessionCreateParams.SubscriptionData.builder()
                        .putMetadata("user_id", userId.toString())
                        .build())
                .build();
    }

    @VisibleForTesting
    SessionCreateParams buildCustomerPortalParams(String customerId) {
        return SessionCreateParams.builder()
                .setCustomer(customerId)
                .setReturnUrl(stripeProperties.getReturnUrl())
                .build();
    }

    @VisibleForTesting
    String getPriceId(PlanType planType, BillingCycle billingCycle) {
        return switch (planType) {
            case PRO -> switch (billingCycle) {
                case MONTHLY -> stripeProperties.getProMonthlyPriceId();
                case YEARLY -> stripeProperties.getProYearlyPriceId();
            };
            case PREMIUM -> switch (billingCycle) {
                case MONTHLY -> stripeProperties.getPremiumMonthlyPriceId();
                case YEARLY -> stripeProperties.getPremiumYearlyPriceId();
            };
            default -> throw new IllegalArgumentException("Invalid plan type for price lookup: " + planType);
        };
    }
}