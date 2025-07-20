package dev.aimusic.backend.subscription;

import dev.aimusic.backend.clients.stripe.StripeService;
import dev.aimusic.backend.config.StripeProperties;
import dev.aimusic.backend.credit.CreditService;
import dev.aimusic.backend.subscription.dao.BillingCycle;
import dev.aimusic.backend.subscription.dao.PlanType;
import dev.aimusic.backend.subscription.dao.SubscriptionDao;
import dev.aimusic.backend.subscription.dto.SubscriptionInfoResponse;
import dev.aimusic.backend.user.dao.UserDao;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionDao subscriptionDao;
    private final UserDao userDao;
    private final CreditService creditService;
    private final StripeService stripeService;
    private final StripeProperties stripeProperties;

    /**
     * 获取用户订阅信息
     */
    @Transactional
    public SubscriptionInfoResponse getUserSubscriptionInfo(Long userId) {
        var subscription = subscriptionDao.findByUserId(userId);

        return SubscriptionInfoResponse.builder()
                .planType(subscription.getPlanType().getName())
                .billingCycle(subscription.getBillingCycle().name())
                .status(subscription.getStatus())
                .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .build();
    }

    /**
     * 创建Stripe checkout session
     */
    public String createCheckoutSession(Long userId, PlanType planType, String billingCycle) {
        var subscription = subscriptionDao.findByUserId(userId);
        return stripeService.createCheckoutSession(
                subscription.getStripeCustomerId(),
                planType,
                billingCycle,
                userId
        );
    }

    /**
     * 创建Stripe customer portal session
     */
    public String createCustomerPortalSession(Long userId) {
        var subscription = subscriptionDao.findByUserId(userId);
        return stripeService.createCustomerPortalSession(subscription.getStripeCustomerId());
    }

    /**
     * 处理订阅创建/更新（webhook调用）
     */
    @Transactional
    public void handleSubscriptionChange(com.stripe.model.Subscription stripeSubscription) {
        var subscriptionItem = stripeSubscription.getItems().getData().get(0);
        var subscription = subscriptionDao.findByStripeCustomerId(stripeSubscription.getCustomer());

        var oldPlanType = subscription.getPlanType();
        var newPlanType = stripeService.getPlanTypeByProductId(
                subscriptionItem.getPlan().getProduct());

        // 更新订阅信息
        subscription.setPlanType(newPlanType);
        subscription.setStatus(stripeSubscription.getStatus().toUpperCase());
        subscription.setCancelAtPeriodEnd(stripeSubscription.getCancelAtPeriodEnd());

        // 转换时间戳
        subscription.setCurrentPeriodStart(
                LocalDateTime.ofInstant(Instant.ofEpochSecond(
                        subscriptionItem.getCurrentPeriodStart()), ZoneOffset.UTC));
        subscription.setCurrentPeriodEnd(
                LocalDateTime.ofInstant(Instant.ofEpochSecond(
                        subscriptionItem.getCurrentPeriodEnd()), ZoneOffset.UTC));

        // 设置产品和价格ID
        subscription.setStripePriceId(subscriptionItem.getPrice().getId());
        subscription.setStripeProductId(subscriptionItem.getPrice().getProduct());

        // 设置billing cycle
        var interval = subscriptionItem.getPrice().getRecurring().getInterval();
        subscription.setBillingCycle("month".equals(interval) ?
                BillingCycle.MONTHLY : BillingCycle.YEARLY);

        // 保存更新后的订阅信息
        subscriptionDao.save(subscription);

        // 如果计划类型改变，重置积分
        if (!Objects.equals(oldPlanType, newPlanType)) {
            creditService.handleSubscriptionChange(subscription.getUserId(), newPlanType);
        }

        log.info("Successfully processed subscription change for user: {}", subscription.getUserId());
    }

    /**
     * 处理订阅取消（webhook调用）
     */
    @Transactional
    public void handleSubscriptionCancellation(com.stripe.model.Subscription stripeSubscription) {
        var subscription = subscriptionDao.findByStripeCustomerId(stripeSubscription.getCustomer());

        // 更新订阅状态为FREE
        subscription.setPlanType(PlanType.FREE);
        subscription.setStatus("CANCELED");
        subscription.setCancelAtPeriodEnd(false);
        subscriptionDao.save(subscription);

        // 重置积分到FREE计划
        creditService.handleSubscriptionChange(subscription.getUserId(), PlanType.FREE);

        log.info("Successfully processed subscription cancellation for user: {}", subscription.getUserId());
    }
}