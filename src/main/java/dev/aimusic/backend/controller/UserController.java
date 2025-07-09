package dev.aimusic.backend.controller;

import dev.aimusic.backend.user.UserService;
import dev.aimusic.backend.user.dao.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController extends AbstractController {

    private final UserService userService;

    @GetMapping("/users/me")
    public UserModel getCurrentUser() {
        return getCurrentUserModel();
    }
}
