package dev.aimusic.backend.user;

import dev.aimusic.backend.user.dao.UserDao;
import dev.aimusic.backend.user.dao.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;

    public UserModel findById(String id) {
        return userDao.findById(id);
    }

    public UserModel createOrUpdate(String provider,
                                    String externalId,
                                    String email,
                                    String name,
                                    String avatarUrl) {
        var user = userDao.findByProviderAndExternalId(provider, externalId);

        if (Objects.nonNull(user)) {
            user.setEmail(email);
            user.setName(name);
            user.setAvatarUrl(avatarUrl);
        } else {
            user = UserModel.builder()
                    .provider(provider)
                    .externalId(externalId)
                    .email(email)
                    .name(name)
                    .avatarUrl(avatarUrl)
                    .build();
        }

        return userDao.save(user);
    }

}
