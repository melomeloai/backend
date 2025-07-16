package dev.aimusic.backend.user.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDao {
    private final UserRepository userRepository;

    public UserModel findByClerkId(String clerkId) {
        return userRepository.findByClerkId(clerkId).orElse(null);
    }

    public UserModel save(UserModel user) {
        return userRepository.save(user);
    }
}
