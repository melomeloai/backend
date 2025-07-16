package dev.aimusic.backend.clients.stripe;

import com.stripe.StripeClient;
import com.stripe.param.CustomerCreateParams;
import dev.aimusic.backend.config.StripeProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeService {
    private final StripeClient stripeClient;

    public StripeService(StripeProperties stripeProperties) {
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

}
