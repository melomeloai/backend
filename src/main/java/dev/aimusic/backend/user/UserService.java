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

    public UserModel findByGoogleSub(String googleSub) {
        return userRepository.findByGoogleSub(googleSub).orElse(null);
    }

    public UserModel createOrUpdateByGoogleInfo(String googleSub,
                                                String email,
                                                String name,
                                                String avatarUrl) {
        var user = userRepository.findByGoogleSub(googleSub).orElse(null);

        if (Objects.nonNull(user)) {
            user.setEmail(email);
            user.setName(name);
            user.setAvatarUrl(avatarUrl);
        } else {
            user = UserModel.builder()
                    .googleSub(googleSub)
                    .email(email)
                    .name(name)
                    .avatarUrl(avatarUrl)
                    .build();
        }
        return userRepository.save(user);
    }
}
