package dev.aimusic.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class R2Config {

    private final R2Properties r2Properties;

    @Bean
    public S3Client s3Client() {
        var credentials = AwsBasicCredentials.create(
                r2Properties.getAccessKey(),
                r2Properties.getSecretKey()
        );

        return S3Client.builder()
                .endpointOverride(URI.create(r2Properties.getEndpoint()))
                .region(Region.of(r2Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var credentials = AwsBasicCredentials.create(
                r2Properties.getAccessKey(),
                r2Properties.getSecretKey()
        );

        return S3Presigner.builder()
                .endpointOverride(URI.create(r2Properties.getEndpoint()))
                .region(Region.of(r2Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}