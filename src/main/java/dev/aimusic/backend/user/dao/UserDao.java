package dev.aimusic.backend.user.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDao {

    private final UserRepository userRepository;

    public Optional<UserModel> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<UserModel> findByClerkId(String clerkId) {
        return userRepository.findByClerkId(clerkId);
    }

    public UserModel save(UserModel user) {
        return userRepository.save(user);
    }
}
