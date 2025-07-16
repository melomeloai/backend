package dev.aimusic.backend.user;

import dev.aimusic.backend.subscription.SubscriptionService;
import dev.aimusic.backend.user.dao.UserDao;
import dev.aimusic.backend.user.dao.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;
    private final SubscriptionService subscriptionService;

    public UserModel findOrCreateUser(String clerkId, String email, String name) {
        // Check if user already exists
        var existingUser = userDao.findByClerkId(clerkId);
        if (Objects.nonNull(existingUser)) {
            return existingUser;
        }

        // Create a new user
        var user = UserModel.builder()
                .clerkId(clerkId)
                .email(email)
                .name(name)
                .build();
        var savedUser = userDao.save(user);

        // Create a subscription for the new user
        subscriptionService.initSubscription(savedUser);

        // Save the user
        return savedUser;
    }

}
