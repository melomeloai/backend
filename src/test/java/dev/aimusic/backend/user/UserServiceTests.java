package dev.aimusic.backend.user;

import dev.aimusic.backend.user.dao.UserModel;
import dev.aimusic.backend.user.dao.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    void test_findById() {
        var userModel = new PodamFactoryImpl().manufacturePojo(UserModel.class);

        doReturn(Optional.of(userModel))
                .when(userRepository).findById(userModel.getId());

        var foundUser = userService.findById(userModel.getId());

        assertEquals(userModel, foundUser);
        verify(userRepository).findById(userModel.getId());
    }

    @Test
    void test_createOrUpdate_create() {
        var provider = RandomStringUtils.secure().nextAlphabetic(10);
        var externalId = RandomStringUtils.secure().nextAlphabetic(10);
        var email = RandomStringUtils.secure().nextAlphabetic(10);
        var name = RandomStringUtils.secure().nextAlphabetic(10);
        var avatarUrl = RandomStringUtils.secure().nextAlphabetic(10);

        doReturn(Optional.empty())
                .when(userRepository).findByProviderAndExternalId(provider, externalId);
        var userModel = new PodamFactoryImpl().manufacturePojo(UserModel.class);
        var captor = ArgumentCaptor.forClass(UserModel.class);
        doReturn(userModel)
                .when(userRepository).save(captor.capture());

        var createdUser = userService.createOrUpdate(provider, externalId, email, name, avatarUrl);
        assertEquals(userModel, createdUser);
        verify(userRepository).findByProviderAndExternalId(provider, externalId);
        var capturedUser = captor.getValue();
        assertEquals(provider, capturedUser.getProvider());
        assertEquals(externalId, capturedUser.getExternalId());
        assertEquals(email, capturedUser.getEmail());
        assertEquals(name, capturedUser.getName());
        assertEquals(avatarUrl, capturedUser.getAvatarUrl());
    }

    @Test
    void test_createOrUpdate_update() {
        var provider = RandomStringUtils.secure().nextAlphabetic(10);
        var externalId = RandomStringUtils.secure().nextAlphabetic(10);
        var email = RandomStringUtils.secure().nextAlphabetic(10);
        var name = RandomStringUtils.secure().nextAlphabetic(10);
        var avatarUrl = RandomStringUtils.secure().nextAlphabetic(10);

        var userModel = mock(UserModel.class);
        doReturn(Optional.of(userModel))
                .when(userRepository).findByProviderAndExternalId(provider, externalId);
        doReturn(userModel)
                .when(userRepository).save(userModel);

        var updatedUser = userService.createOrUpdate(provider, externalId, email, name, avatarUrl);
        assertEquals(userModel, updatedUser);
        verify(userRepository).findByProviderAndExternalId(provider, externalId);
        verify(userRepository).save(userModel);
        verify(userModel).setEmail(email);
        verify(userModel).setName(name);
        verify(userModel).setAvatarUrl(avatarUrl);
    }
}
