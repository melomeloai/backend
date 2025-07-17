package dev.aimusic.backend.clients.stripe;

import com.stripe.StripeClient;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.billingportal.SessionCreateParams;
import dev.aimusic.backend.config.StripeProperties;
import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION;

@Service
@Slf4j
public class StripeService {
    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;

    public StripeService(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
        this.stripeClient = new StripeClient(stripeProperties.getApiKey());
    }

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

    public String createSubscriptionPortalSession(
            String customerId,
            String returnUrl) {
        var sessionParams = SessionCreateParams.builder()
                .setCustomer(customerId)
                .setReturnUrl(returnUrl)
                .build();
        try {
            var session = stripeClient.billingPortal().sessions().create(sessionParams);
            return session.getUrl();
        } catch (Exception e) {
            // Fail the request if portal session creation fails
            throw new RuntimeException("Failed to create Stripe portal session", e);
        }
    }

    public String createSubscriptionCheckoutSession(
            String customerId,
            PlanType planType,
            String successUrl,
            String cancelUrl) {
        var priceId = stripeProperties.getMonthlyPriceIds().get(planType);
        var sessionParams = com.stripe.param.checkout.SessionCreateParams.builder()
                .addLineItem(
                        com.stripe.param.checkout.SessionCreateParams.LineItem
                                .builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                )
                .setMode(SUBSCRIPTION)
                .setCustomer(customerId)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .build();
        try {
            var session = stripeClient.checkout().sessions().create(sessionParams);
            return session.getUrl();
        } catch (Exception e) {
            // Fail the request if checkout session creation fails
            throw new RuntimeException("Failed to create Stripe checkout session", e);
        }
    }
}
