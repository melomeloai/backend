package dev.aimusic.backend.controller;

import dev.aimusic.backend.auth.AuthenticationUtils;
import dev.aimusic.backend.clients.clerk.ClerkService;
import dev.aimusic.backend.common.exceptions.AuthenticationException;
import dev.aimusic.backend.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.aimusic.backend.common.Constants.API_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = API_PATH + "/auth", produces = {APPLICATION_JSON_VALUE})
public class AuthController {

    private final ClerkService clerkService;
    private final UserService userService;

    @PostMapping("/login-callback")
    public ResponseEntity<Void> loginCallback(HttpServletRequest request) {
        var token = AuthenticationUtils.extractTokenFromRequest(request);
        if (StringUtils.isBlank(token)) {
            throw new AuthenticationException("Missing or invalid Authorization header");
        }

        // 验证并解析JWT token
        var tokenModel = clerkService.authenticate(token);

        var clerkId = tokenModel.getSub();
        var email = tokenModel.getEmail();
        var name = tokenModel.getName();

        if (StringUtils.isBlank(clerkId)) {
            throw new AuthenticationException("Invalid token: missing subject");
        }

        if (StringUtils.isBlank(email)) {
            throw new AuthenticationException("Invalid token: missing email");
        }

        // 查找或创建用户
        userService.findOrCreateUser(clerkId, email, name);

        return ResponseEntity.ok().build();
    }
}