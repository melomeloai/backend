package dev.aimusic.backend.controller;

import dev.aimusic.backend.user.UserService;
import dev.aimusic.backend.user.dao.UserModel;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import static dev.aimusic.backend.common.Constants.SESSION_USER_ID;


public class AbstractController {
    @Autowired
    private UserService userService;
    @Autowired
    private HttpSession session;

    public UserModel getCurrentUserModel() {
        var userId = (String) session.getAttribute(SESSION_USER_ID);
        if (Objects.isNull(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }
        var userModel = userService.findById(userId);
        if (Objects.isNull(userModel)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        return userModel;
    }
}
