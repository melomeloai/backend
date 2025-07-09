package dev.aimusic.backend.controller;

import dev.aimusic.backend.auth.AuthService;
import dev.aimusic.backend.config.AbstractController;
import dev.aimusic.backend.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import static dev.aimusic.backend.common.Constants.SESSION_USER_ID;

@RestController
@RequiredArgsConstructor
public class AuthController extends AbstractController {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping("/auth/oauth2/success")
    public RedirectView handleLoginSuccess(OAuth2AuthenticationToken auth, HttpSession session) {
        var authUserModel = authService.getAuthUserModel(auth);

        var userModel = userService.createOrUpdate(
                authUserModel.getProvider(),
                authUserModel.getExternalId(),
                authUserModel.getEmail(),
                authUserModel.getName(),
                authUserModel.getAvatarUrl()
        );

        session.setAttribute(SESSION_USER_ID, userModel.getId());

        return new RedirectView("/users/me");
    }
}
