package dev.aimusic.backend.user;

import dev.aimusic.backend.user.dao.UserModel;
import dev.aimusic.backend.user.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserModel findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserModel createOrUpdate(String provider,
                                    String externalId,
                                    String email,
                                    String name,
                                    String avatarUrl) {
        var user = userRepository.findByProviderAndExternalId(provider, externalId)
                .orElse(null);

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

        return userRepository.save(user);
    }

}
