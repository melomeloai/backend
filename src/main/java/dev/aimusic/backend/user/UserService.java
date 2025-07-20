package dev.aimusic.backend.user;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.clients.stripe.StripeService;
import dev.aimusic.backend.common.exceptions.NotFoundException;
import dev.aimusic.backend.credit.dao.CreditDao;
import dev.aimusic.backend.subscription.dao.SubscriptionDao;
import dev.aimusic.backend.user.dao.UserDao;
import dev.aimusic.backend.user.dao.UserModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserDao userDao;
    private final SubscriptionDao subscriptionDao;
    private final CreditDao creditDao;
    private final StripeService stripeService;

    /**
     * 初始化新用户（在用户首次登录时调用）
     */
    @Transactional
    public UserModel findOrCreateUser(String clerkId, String email, String name) {
        // 1. 查找或创建用户
        try {
            return userDao.findByClerkId(clerkId);
        } catch (NotFoundException e) {
            log.info("User not found, creating new user with clerkId: {}", clerkId);
            return createUser(clerkId, email, name);
        }
    }

    @VisibleForTesting
    UserModel createUser(String clerkId, String email, String name) {
        var user = userDao.save(UserModel.builder()
                .clerkId(clerkId)
                .email(email)
                .name(name)
                .build());

        // 创建Stripe customer
        var stripeCustomerId = stripeService.createCustomer(email);

        // 创建默认订阅记录
        subscriptionDao.createDefaultSubscription(user.getId(), stripeCustomerId);

        // 5. 创建默认积分记录
        creditDao.createDefaultCredit(user.getId());

        log.info("Successfully initialized new user: {}, email: {}", user.getId(), email);
        return user;
    }
}
