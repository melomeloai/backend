package dev.aimusic.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "r2")
public class R2Properties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String region = "auto";
    private String publicDomain;
}