package dev.aimusic.backend.controller;

import dev.aimusic.backend.auth.UserContext;
import dev.aimusic.backend.common.ApiResponse;
import dev.aimusic.backend.user.UserService;
import dev.aimusic.backend.user.transformer.UserResponse;
import dev.aimusic.backend.user.transformer.UserTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> getUserMe() {
        var userModel = UserContext.get();
        if (Objects.isNull(userModel)) {
            return ResponseEntity.notFound()
                    .build();
        }
        return ResponseEntity.ok(
                ApiResponse.ok(
                        UserTransformer.toUserResponse(userModel)));
    }
}
