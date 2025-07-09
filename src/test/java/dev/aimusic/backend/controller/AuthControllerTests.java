package dev.aimusic.backend.controller;

import dev.aimusic.backend.auth.AuthService;
import dev.aimusic.backend.auth.dao.AuthUserModel;
import dev.aimusic.backend.user.UserService;
import dev.aimusic.backend.user.dao.UserModel;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static dev.aimusic.backend.common.Constants.SESSION_USER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTests {
    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;

    @Test
    void test_handleLoginSuccess() {
        var token = new PodamFactoryImpl().manufacturePojo(OAuth2AuthenticationToken.class);
        var session = mock(HttpSession.class);

        var authUserModel = new PodamFactoryImpl().manufacturePojo(AuthUserModel.class);
        doReturn(authUserModel).when(authService).getAuthUserModel(token);

        var userModel = new PodamFactoryImpl().manufacturePojo(UserModel.class);
        doReturn(userModel).when(userService).createOrUpdate(
                authUserModel.getProvider(),
                authUserModel.getExternalId(),
                authUserModel.getEmail(),
                authUserModel.getName(),
                authUserModel.getAvatarUrl()
        );

        var redirectView = authController.handleLoginSuccess(token, session);

        assertEquals("/users/me", redirectView.getUrl());
        verify(authService).getAuthUserModel(token);
        verify(userService).createOrUpdate(
                authUserModel.getProvider(),
                authUserModel.getExternalId(),
                authUserModel.getEmail(),
                authUserModel.getName(),
                authUserModel.getAvatarUrl()
        );
        verify(session).setAttribute(SESSION_USER_ID, userModel.getId());
    }
}
