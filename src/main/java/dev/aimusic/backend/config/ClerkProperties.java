package dev.aimusic.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "clients.clerk")
public class ClerkProperties {
    private String apiKey;
    private String jwtIssuer;
}
