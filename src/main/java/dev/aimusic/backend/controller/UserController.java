package dev.aimusic.backend.controller;

import dev.aimusic.backend.auth.AuthenticationUtils;
import dev.aimusic.backend.user.UserService;
import dev.aimusic.backend.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.aimusic.backend.common.Constants.API_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = API_PATH + "/users", produces = {APPLICATION_JSON_VALUE})
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserInfo(Authentication auth) {
        var userModel = AuthenticationUtils.getUser(auth);
        return ResponseEntity.ok(UserResponse.builder()
                .id(userModel.getId())
                .email(userModel.getEmail())
                .name(userModel.getName())
                .createdAt(userModel.getCreatedAt())
                .build());
    }
}
