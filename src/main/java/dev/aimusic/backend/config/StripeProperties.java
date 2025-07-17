package dev.aimusic.backend.config;

import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "clients.stripe")
public class StripeProperties {
    private String apiKey;
    private Map<PlanType, String> monthlyPriceIds;
}
