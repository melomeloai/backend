package dev.aimusic.backend.controller;

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

    private final UserService userService;

    @GetMapping("/auth/oauth2/success")
    public RedirectView handleLoginSuccess(OAuth2AuthenticationToken auth, HttpSession session) {
        var attrs = auth.getPrincipal().getAttributes();

        var sub = (String) attrs.get("sub");
        var email = (String) attrs.get("email");
        var name = (String) attrs.get("name");
        var avatar = (String) attrs.get("picture");

        var userModel = userService.createOrUpdateByGoogleInfo(sub, email, name, avatar);
        session.setAttribute(SESSION_USER_ID, userModel.getId());

        return new RedirectView("/users/me");
    }
}
