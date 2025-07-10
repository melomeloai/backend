package dev.aimusic.backend.user;

import dev.aimusic.backend.user.dao.UserDao;
import dev.aimusic.backend.user.dao.UserModel;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserDao userDao;

    @Test
    void test_findById() {
        var userModel = new PodamFactoryImpl().manufacturePojo(UserModel.class);

        doReturn(userModel).when(userDao).findById(userModel.getId());

        var foundUser = userService.findById(userModel.getId());

        assertEquals(userModel, foundUser);
        verify(userDao).findById(userModel.getId());
    }

    @Test
    void test_createOrUpdate_create() {
        var provider = RandomStringUtils.secure().nextAlphabetic(10);
        var externalId = RandomStringUtils.secure().nextAlphabetic(10);
        var email = RandomStringUtils.secure().nextAlphabetic(10);
        var name = RandomStringUtils.secure().nextAlphabetic(10);
        var avatarUrl = RandomStringUtils.secure().nextAlphabetic(10);

        doReturn(null)
                .when(userDao).findByProviderAndExternalId(provider, externalId);
        var userModel = new PodamFactoryImpl().manufacturePojo(UserModel.class);
        var captor = ArgumentCaptor.forClass(UserModel.class);
        doReturn(userModel)
                .when(userDao).save(captor.capture());

        var createdUser = userService.createOrUpdate(provider, externalId, email, name, avatarUrl);
        assertEquals(userModel, createdUser);
        verify(userDao).findByProviderAndExternalId(provider, externalId);
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
        doReturn(userModel)
                .when(userDao).findByProviderAndExternalId(provider, externalId);
        doReturn(userModel)
                .when(userDao).save(userModel);

        var updatedUser = userService.createOrUpdate(provider, externalId, email, name, avatarUrl);
        assertEquals(userModel, updatedUser);
        verify(userDao).findByProviderAndExternalId(provider, externalId);
        verify(userDao).save(userModel);
        verify(userModel).setEmail(email);
        verify(userModel).setName(name);
        verify(userModel).setAvatarUrl(avatarUrl);
    }
}
