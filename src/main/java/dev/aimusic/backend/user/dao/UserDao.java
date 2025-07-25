package dev.aimusic.backend.user.dao;

import dev.aimusic.backend.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "users")
public class UserDao {

    private final UserRepository userRepository;

    public UserModel findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @Cacheable(key = "#clerkId")
    public UserModel findByClerkId(String clerkId) {
        return userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new NotFoundException("User not found with Clerk ID: " + clerkId));
    }

    @CacheEvict(key = "#user.clerkId")
    public UserModel save(UserModel user) {
        return userRepository.save(user);
    }
}
