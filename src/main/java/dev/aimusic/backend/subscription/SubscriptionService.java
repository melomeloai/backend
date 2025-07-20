package dev.aimusic.backend.subscription;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.clients.stripe.StripeService;
import dev.aimusic.backend.config.StripeProperties;
import dev.aimusic.backend.credit.CreditService;
import dev.aimusic.backend.subscription.dao.PlanType;
import dev.aimusic.backend.subscription.dao.SubscriptionDao;
import dev.aimusic.backend.subscription.dao.SubscriptionModel;
import dev.aimusic.backend.subscription.dto.SubscriptionInfoResponse;
import dev.aimusic.backend.user.dao.UserDao;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
        var subscription = subscriptionDao.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Subscription not found for user: " + userId));

        return SubscriptionInfoResponse.builder()
                .planType(subscription.getPlanType().getName())
                .billingCycle(subscription.getBillingCycle())
                .status(subscription.getStatus())
                .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .nextResetTime(subscription.getNextResetTime())
                .build();
    }

    /**
     * 创建Stripe checkout session
     */
    public String createCheckoutSession(Long userId, PlanType planType, String billingCycle) {
        var subscription = getSubscriptionByUserId(userId);

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
        var subscription = getSubscriptionByUserId(userId);

        if (subscription.getStripeCustomerId() == null) {
            throw new IllegalStateException("No Stripe customer ID found for user: " + userId);
        }

        return stripeService.createCustomerPortalSession(subscription.getStripeCustomerId());
    }

    /**
     * 处理订阅创建/更新（webhook调用）
     */
    @Transactional
    public void handleSubscriptionChange(com.stripe.model.Subscription stripeSubscription) {
        var subscription = subscriptionDao.findByStripeCustomerId(stripeSubscription.getCustomer())
                .orElse(null);
        if (subscription == null) {
            log.warn("No local subscription found for Stripe subscription: {}", stripeSubscription.getId());
            return;
        }

        var oldPlanType = subscription.getPlanType();
        var newPlanType = parsePlanTypeFromStripeSubscription(stripeSubscription);

        // 更新订阅信息
        updateSubscriptionFromStripe(subscription, stripeSubscription, newPlanType);
        subscriptionDao.save(subscription);

        // 如果计划类型改变，重置积分
        if (!oldPlanType.equals(newPlanType)) {
            creditService.handleSubscriptionChange(subscription.getUserId(), newPlanType);
        }

        log.info("Successfully processed subscription change for user: {}", subscription.getUserId());
    }

    /**
     * 处理订阅取消（webhook调用）
     */
    @Transactional
    public void handleSubscriptionCancellation(com.stripe.model.Subscription stripeSubscription) {
        var subscription = subscriptionDao.findByStripeCustomerId(stripeSubscription.getCustomer())
                .orElse(null);
        if (subscription == null) {
            return;
        }

        // 更新订阅状态为FREE
        subscription.setPlanType(PlanType.FREE);
        subscription.setStatus("CANCELED");
        subscription.setCancelAtPeriodEnd(false);
        subscription.setNextResetTime(LocalDateTime.now().plusDays(1)); // FREE plan每日重置

        subscriptionDao.save(subscription);

        // 重置积分到FREE计划（每日10积分）
        creditService.handleSubscriptionChange(subscription.getUserId(), PlanType.FREE);

        log.info("Successfully processed subscription cancellation for user: {}", subscription.getUserId());
    }

    // ===== 辅助方法（使用@VisibleForTesting便于单元测试） =====
    @VisibleForTesting
    SubscriptionModel getSubscriptionByUserId(Long userId) {
        return subscriptionDao.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Subscription not found for user: " + userId));
    }

    @VisibleForTesting
    PlanType parsePlanTypeFromStripeSubscription(com.stripe.model.Subscription stripeSubscription) {
        // 从Stripe订阅的product解析计划类型
        var productId = stripeSubscription.getItems().getData().get(0).getPlan().getProduct();

        if (StringUtils.equals(productId, stripeProperties.getProProductId())) {
            return PlanType.PRO;
        } else if (StringUtils.equals(productId, stripeProperties.getPremiumProductId())) {
            return PlanType.PREMIUM;
        } else {
            return PlanType.FREE;
        }
    }

    @VisibleForTesting
    void updateSubscriptionFromStripe(SubscriptionModel subscription, com.stripe.model.Subscription stripeSubscription, PlanType newPlanType) {
        subscription.setPlanType(newPlanType);
        subscription.setStatus(stripeSubscription.getStatus().toUpperCase());
        subscription.setCancelAtPeriodEnd(stripeSubscription.getCancelAtPeriodEnd());

        // 转换时间戳
        subscription.setCurrentPeriodStart(
                LocalDateTime.ofInstant(Instant.ofEpochSecond(stripeSubscription.getStartDate()), ZoneOffset.UTC));
        if (stripeSubscription.getEndedAt() != null) {
            subscription.setNextResetTime(
                    LocalDateTime.ofInstant(Instant.ofEpochSecond(stripeSubscription.getEndedAt()), ZoneOffset.UTC));
        }

        // 设置产品和价格ID
        var item = stripeSubscription.getItems().getData().get(0);
        subscription.setStripePriceId(item.getPrice().getId());
        subscription.setStripeProductId(item.getPrice().getProduct());

        // 设置billing cycle
        var interval = item.getPrice().getRecurring().getInterval();
        subscription.setBillingCycle("month".equals(interval) ? "monthly" : "yearly");
    }
}