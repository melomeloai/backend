package dev.aimusic.backend.credit;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.credit.dao.CreditDao;
import dev.aimusic.backend.credit.dao.CreditModel;
import dev.aimusic.backend.credit.dao.TriggerSource;
import dev.aimusic.backend.credit.dto.CreditCheckResult;
import dev.aimusic.backend.credit.dto.CreditInfoResponse;
import dev.aimusic.backend.subscription.dao.PlanType;
import dev.aimusic.backend.subscription.dao.SubscriptionDao;
import dev.aimusic.backend.subscription.dao.SubscriptionModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreditService {

    private final CreditDao creditDao;
    private final SubscriptionDao subscriptionDao;
    private final CreditLogger creditLogger;

    /**
     * 检查用户是否有足够积分
     */
    public CreditCheckResult checkCredits(Long userId, Integer requiredCredits, TriggerSource triggerSource) {
        // 1. 确保用户有积分记录
        var creditModel = getOrCreateCreditRecord(userId);

        // 2. 懒重置积分
        resetCreditsIfNeeded(userId);

        // 3. 重新获取积分信息（可能已被重置）
        creditModel = creditDao.findByUserId(userId).orElseThrow();

        // 4. 获取订阅信息
        var subscription = subscriptionDao.findByUserId(userId).orElseThrow();

        // 5. 计算可用积分
        var availableCredits = calculateAvailableCredits(creditModel, triggerSource);

        // 6. 检查是否足够
        if (availableCredits >= requiredCredits) {
            return CreditCheckResult.sufficient(
                    creditModel.getPermanentCredits(),
                    creditModel.getRenewableCredits(),
                    subscription.getPlanType().getName()
            );
        } else {
            var errorMessage = String.format("Insufficient credits. Required: %d, Available: %d",
                    requiredCredits, availableCredits);
            return CreditCheckResult.insufficient(
                    creditModel.getPermanentCredits(),
                    creditModel.getRenewableCredits(),
                    subscription.getPlanType().getName(),
                    errorMessage
            );
        }
    }

    /**
     * 消耗用户积分
     */
    public boolean consumeCredits(Long userId, Integer credits, TriggerSource triggerSource, String taskType) {
        // 1. 再次检查积分是否足够（防止并发问题）
        var checkResult = checkCredits(userId, credits, triggerSource);
        if (!checkResult.getSufficient()) {
            log.warn("Credit consumption failed for user {}: {}", userId, checkResult.getErrorMessage());
            return false;
        }

        // 2. 获取积分记录
        var creditModel = creditDao.findByUserId(userId).orElseThrow();

        // 3. 根据触发源执行消耗策略
        var consumeResult = executeConsumptionStrategy(creditModel, credits, triggerSource);
        if (!consumeResult) {
            log.error("Failed to execute consumption strategy for user: {}", userId);
            return false;
        }

        // 4. 保存更新后的积分
        creditDao.save(creditModel);

        // 5. 记录日志
        creditLogger.logCreditConsumption(userId, credits, triggerSource, taskType,
                creditModel.getPermanentCredits(), creditModel.getRenewableCredits());

        return true;
    }

    /**
     * 重置用户积分（懒重置机制）
     */
    public void resetCreditsIfNeeded(Long userId) {
        var subscription = subscriptionDao.findByUserId(userId).orElse(null);
        if (subscription == null) {
            log.warn("No subscription found for user: {}", userId);
            return;
        }

        var now = LocalDateTime.now();
        if (subscription.getNextResetTime() == null || now.isBefore(subscription.getNextResetTime())) {
            return; // 还没到重置时间
        }

        // 执行重置
        var creditModel = creditDao.findByUserId(userId).orElseThrow();
        var planType = subscription.getPlanType();

        // 重置积分数量
        creditModel.setRenewableCredits(planType.getResetAmount());
        creditModel.setLastResetTime(now);

        // 计算下次重置时间
        var nextResetTime = calculateNextResetTime(now, planType);
        subscription.setNextResetTime(nextResetTime);

        // 保存更新
        creditDao.save(creditModel);
        subscriptionDao.save(subscription);

        creditLogger.logCreditReset(userId, planType, planType.getResetAmount(), nextResetTime);
    }

    /**
     * 获取用户积分详情
     */
    @Transactional
    public CreditInfoResponse getUserCreditInfo(Long userId) {
        // 1. 懒重置积分
        resetCreditsIfNeeded(userId);

        // 2. 获取积分和订阅信息
        var creditModel = getOrCreateCreditRecord(userId);
        var subscription = getOrCreateSubscriptionRecord(userId);

        return CreditInfoResponse.builder()
                .permanentCredits(creditModel.getPermanentCredits())
                .renewableCredits(creditModel.getRenewableCredits())
                .nextResetTime(subscription.getNextResetTime())
                .planType(subscription.getPlanType().getName())
                .build();
    }

    /**
     * 处理订阅变更时的积分重置
     */
    public void handleSubscriptionChange(Long userId, PlanType newPlanType) {
        var creditModel = getOrCreateCreditRecord(userId);
        var subscription = subscriptionDao.findByUserId(userId).orElseThrow();

        var oldPlanType = subscription.getPlanType();
        var now = LocalDateTime.now();

        // 立即重置积分
        creditModel.setRenewableCredits(newPlanType.getResetAmount());
        creditModel.setLastResetTime(now);

        // 计算下次重置时间
        var nextResetTime = calculateNextResetTime(now, newPlanType);
        subscription.setNextResetTime(nextResetTime);

        // 保存更新
        creditDao.save(creditModel);
        subscriptionDao.save(subscription);

        creditLogger.logSubscriptionChange(userId, oldPlanType, newPlanType, "PLAN_CHANGE_RESET");
    }

    // ===== 辅助方法（使用@VisibleForTesting便于单元测试） =====

    @VisibleForTesting
    CreditModel getOrCreateCreditRecord(Long userId) {
        return creditDao.findByUserId(userId)
                .orElseGet(() -> creditDao.createDefaultCredit(userId));
    }

    @VisibleForTesting
    SubscriptionModel getOrCreateSubscriptionRecord(Long userId) {
        return subscriptionDao.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Subscription record not found for user: " + userId));
    }

    @VisibleForTesting
    Integer calculateAvailableCredits(CreditModel creditModel, TriggerSource triggerSource) {
        return switch (triggerSource) {
            case UI -> creditModel.getRenewableCredits() + creditModel.getPermanentCredits();
            case API -> creditModel.getPermanentCredits(); // API只能使用永久积分
        };
    }

    @VisibleForTesting
    boolean executeConsumptionStrategy(CreditModel creditModel, Integer credits, TriggerSource triggerSource) {
        return switch (triggerSource) {
            case UI -> consumeCreditsForUI(creditModel, credits);
            case API -> consumeCreditsForAPI(creditModel, credits);
        };
    }

    @VisibleForTesting
    boolean consumeCreditsForUI(CreditModel creditModel, Integer credits) {
        var remainingCredits = credits;

        // 优先消耗可重置积分
        if (creditModel.getRenewableCredits() > 0) {
            var renewableToConsume = Math.min(remainingCredits, creditModel.getRenewableCredits());
            creditModel.setRenewableCredits(creditModel.getRenewableCredits() - renewableToConsume);
            remainingCredits -= renewableToConsume;
        }

        // 如果还有剩余，消耗永久积分
        if (remainingCredits > 0) {
            if (creditModel.getPermanentCredits() < remainingCredits) {
                return false; // 永久积分不足
            }
            creditModel.setPermanentCredits(creditModel.getPermanentCredits() - remainingCredits);
        }

        return true;
    }

    @VisibleForTesting
    boolean consumeCreditsForAPI(CreditModel creditModel, Integer credits) {
        // API只能消耗永久积分
        if (creditModel.getPermanentCredits() < credits) {
            return false;
        }
        creditModel.setPermanentCredits(creditModel.getPermanentCredits() - credits);
        return true;
    }

    @VisibleForTesting
    LocalDateTime calculateNextResetTime(LocalDateTime baseTime, PlanType planType) {
        return switch (planType.getResetPeriod()) {
            case "day" -> baseTime.plusDays(1);
            case "month" -> baseTime.plusMonths(1);
            default -> throw new IllegalArgumentException("Unknown reset period: " + planType.getResetPeriod());
        };
    }
}