package dev.aimusic.backend.user.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDao {
    private final UserRepository userRepository;

    public UserModel findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserModel findByProviderAndExternalId(String provider, String externalId) {
        return userRepository.findByProviderAndExternalId(provider, externalId).orElse(null);
    }

    public UserModel save(UserModel user) {
        return userRepository.save(user);
    }

}
