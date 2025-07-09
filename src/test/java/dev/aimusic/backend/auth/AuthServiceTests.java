package dev.aimusic.backend.auth;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.Map;

import static dev.aimusic.backend.auth.AuthService.GOOGLE_REGISTRATION_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @InjectMocks
    private AuthService authService;

    @Test
    void test_getAuthUserModel() {
        var provider = RandomStringUtils.secure().nextAlphabetic(10);
        var sub = RandomStringUtils.secure().nextAlphabetic(10);
        var email = RandomStringUtils.secure().nextAlphabetic(10);
        var name = RandomStringUtils.secure().nextAlphabetic(10);
        var picture = RandomStringUtils.secure().nextAlphabetic(10);
        Map<String, Object> attributes = Map.of(
                "email", email,
                "name", name
        );

        var spyService = spy(authService);
        doReturn(sub).when(spyService).extractExternalId(provider, attributes);
        doReturn(picture).when(spyService).extractAvatarUrl(provider, attributes);

        var mockToken = mock(OAuth2AuthenticationToken.class, RETURNS_DEEP_STUBS);
        when(mockToken.getAuthorizedClientRegistrationId()).thenReturn(provider);
        when(mockToken.getPrincipal().getAttributes()).thenReturn(attributes);

        var authUserModel = spyService.getAuthUserModel(mockToken);

        assertEquals(provider, authUserModel.getProvider());
        assertEquals(sub, authUserModel.getExternalId());
        assertEquals(email, authUserModel.getEmail());
        assertEquals(name, authUserModel.getName());
        assertEquals(picture, authUserModel.getAvatarUrl());
        verify(spyService).extractExternalId(provider, attributes);
        verify(spyService).extractAvatarUrl(provider, attributes);
    }

    @Test
    void test_extractExternalId_google() {
        var sub = RandomStringUtils.secure().nextAlphabetic(10);
        Map<String, Object> attrs = Map.of("sub", sub);

        var externalId = authService.extractExternalId(GOOGLE_REGISTRATION_ID, attrs);
        assertEquals(sub, externalId);
    }

    @Test
    void test_extractExternalId_github() {
        var id = RandomStringUtils.secure().nextAlphabetic(10);
        Map<String, Object> attrs = Map.of("id", id);

        var externalId = authService.extractExternalId(AuthService.GITHUB_REGISTRATION_ID, attrs);
        assertEquals(id, externalId);
    }

    @Test
    void test_extractExternalId_apple() {
        var sub = RandomStringUtils.secure().nextAlphabetic(10);
        Map<String, Object> attrs = Map.of("sub", sub);

        var externalId = authService.extractExternalId(AuthService.APPLE_REGISTRATION_ID, attrs);
        assertEquals(sub, externalId);
    }

    @Test
    void test_extractExternalId_unsupportedProvider() {
        var provider = RandomStringUtils.secure().nextAlphabetic(10);
        Map<String, Object> attrs = Map.of("id", RandomStringUtils.secure().nextAlphabetic(10));

        assertThrows(RuntimeException.class, () -> authService.extractExternalId(provider, attrs));
    }

    @Test
    void test_extractAvatarUrl_google() {
        var picture = RandomStringUtils.secure().nextAlphabetic(10);
        Map<String, Object> attrs = Map.of("picture", picture);

        var avatarUrl = authService.extractAvatarUrl(GOOGLE_REGISTRATION_ID, attrs);
        assertEquals(picture, avatarUrl);
    }

    @Test
    void test_extractAvatarUrl_github() {
        var avatarUrl = RandomStringUtils.secure().nextAlphabetic(10);
        Map<String, Object> attrs = Map.of("avatar_url", avatarUrl);

        var result = authService.extractAvatarUrl(AuthService.GITHUB_REGISTRATION_ID, attrs);
        assertEquals(avatarUrl, result);
    }

    @Test
    void test_extractAvatarUrl_apple() {
        Map<String, Object> attrs = Map.of("sub", RandomStringUtils.secure().nextAlphabetic(10));

        var result = authService.extractAvatarUrl(AuthService.APPLE_REGISTRATION_ID, attrs);
        assertNull(result);
    }

    @Test
    void test_extractAvatarUrl_unsupportedProvider() {
        var provider = RandomStringUtils.secure().nextAlphabetic(10);
        Map<String, Object> attrs = Map.of("id", RandomStringUtils.secure().nextAlphabetic(10));

        assertThrows(RuntimeException.class, () -> authService.extractAvatarUrl(provider, attrs));
    }
}
