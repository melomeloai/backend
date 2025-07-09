package dev.aimusic.backend.controller;

import dev.aimusic.backend.user.dao.UserModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {
    @InjectMocks
    private UserController userController;

    @Test
    void test_getCurrentUser() {
        var userModel = new PodamFactoryImpl().manufacturePojo(UserModel.class);
        var spyController = spy(userController);
        doReturn(userModel).when(spyController).getCurrentUserModel();

        var result = spyController.getCurrentUser();
        assertEquals(userModel, result);
    }
}
