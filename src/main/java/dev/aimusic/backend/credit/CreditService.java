package dev.aimusic.backend.credit;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.credit.dao.CreditDao;
import dev.aimusic.backend.credit.dto.CreditInfoResponse;
import dev.aimusic.backend.credit_transcation.CreditLogger;
import dev.aimusic.backend.subscription.dao.PlanType;
import dev.aimusic.backend.subscription.dao.SubscriptionDao;
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
     * 获取用户积分详情
     */
    @Transactional
    public CreditInfoResponse getUserCreditInfo(Long userId) {
        // 懒重置机制：在获取积分信息时检查是否需要重置积分
        resetCreditsIfNeeded(userId);

        var creditModel = creditDao.findByUserId(userId);

        return CreditInfoResponse.builder()
                .permanentCredits(creditModel.getPermanentCredits())
                .renewableCredits(creditModel.getRenewableCredits())
                .nextResetTime(creditModel.getNextResetTime())
                .build();
    }

    /**
     * 重置用户积分（懒重置机制）
     */
    public void resetCreditsIfNeeded(Long userId) {
        var creditModel = creditDao.findByUserId(userId);

        var now = LocalDateTime.now();
        if (creditModel.getNextResetTime() == null || now.isBefore(creditModel.getNextResetTime())) {
            // 还没到重置时间
            return;
        }

        // 执行重置
        var subscription = subscriptionDao.findByUserId(userId);
        var planType = subscription.getPlanType();

        // 重置积分数量和重置时间
        var resetAmount = planType.getResetAmount();
        var nextResetTime = calculateNextResetTime(now, planType);
        creditModel.setRenewableCredits(resetAmount);
        creditModel.setLastResetTime(now);
        creditModel.setNextResetTime(nextResetTime);

        // 保存更新
        creditDao.save(creditModel);

        // 记录重置日志
        creditLogger.logCreditReset(userId, planType, resetAmount, nextResetTime);
    }

    /**
     * 处理订阅变更时的积分重置
     */
    public void handleSubscriptionChange(Long userId, PlanType newPlanType) {
        var creditModel = creditDao.findByUserId(userId);

        // 立即重置积分
        var now = LocalDateTime.now();
        var resetAmount = newPlanType.getResetAmount();
        var nextResetTime = calculateNextResetTime(now, newPlanType);
        creditModel.setRenewableCredits(resetAmount);
        creditModel.setLastResetTime(now);
        creditModel.setNextResetTime(nextResetTime);

        // 保存credit记录和订阅记录
        creditDao.save(creditModel);
        creditLogger.logCreditReset(userId, newPlanType, resetAmount, nextResetTime);
    }

    // ===== 辅助方法（使用@VisibleForTesting便于单元测试） =====

    @VisibleForTesting
    LocalDateTime calculateNextResetTime(LocalDateTime baseTime, PlanType planType) {
        return switch (planType.getResetPeriod()) {
            case "day" -> baseTime.plusDays(1);
            case "month" -> baseTime.plusMonths(1);
            default -> throw new IllegalArgumentException("Unknown reset period: " + planType.getResetPeriod());
        };
    }
}