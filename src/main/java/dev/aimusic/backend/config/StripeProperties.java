package dev.aimusic.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {
    private String secretKey;
    private String webhookSecret;
    private String successUrl;
    private String cancelUrl;
    private String returnUrl;

    private String proProductId;
    private String proMonthlyPriceId;
    private String proYearlyPriceId;

    private String premiumProductId;
    private String premiumMonthlyPriceId;
    private String premiumYearlyPriceId;
}
