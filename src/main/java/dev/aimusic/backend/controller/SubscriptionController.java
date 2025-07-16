package dev.aimusic.backend.controller;

import dev.aimusic.backend.auth.UserContext;
import dev.aimusic.backend.common.ApiResponse;
import dev.aimusic.backend.subscription.SubscriptionService;
import dev.aimusic.backend.subscription.transformer.SubscriptionResponse;
import dev.aimusic.backend.subscription.transformer.SubscriptionTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/users/me/subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getUserMeSubscription() {
        var userModel = UserContext.get();
        if (Objects.isNull(userModel)) {
            return ResponseEntity.notFound()
                    .build();
        }
        var subscriptionModel = subscriptionService
                .getSubscriptionByUserId(userModel.getId());
        if (Objects.isNull(subscriptionModel)) {
            return ResponseEntity.notFound()
                    .build();
        }
        return ResponseEntity.ok(
                ApiResponse.ok(
                        SubscriptionTransformer.toSubscriptionResponse(
                                subscriptionModel)));
    }
}
