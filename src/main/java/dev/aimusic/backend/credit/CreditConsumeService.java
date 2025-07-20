package dev.aimusic.backend.credit;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.credit.dao.CreditDao;
import dev.aimusic.backend.credit.dao.CreditModel;
import dev.aimusic.backend.credit.dto.CreditCheckResult;
import dev.aimusic.backend.credit_transcation.CreditLogger;
import dev.aimusic.backend.credit_transcation.dao.TriggerSource;
import dev.aimusic.backend.subscription.dao.SubscriptionDao;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreditConsumeService {

    private final CreditService creditService;
    private final CreditDao creditDao;
    private final SubscriptionDao subscriptionDao;
    private final CreditLogger creditLogger;

    /**
     * 检查用户是否有足够积分
     */
    public CreditCheckResult checkCredits(Long userId, Integer requiredCredits, TriggerSource triggerSource) {
        var creditModel = creditDao.findByUserId(userId);

        // 2. 懒重置积分
        creditService.resetCreditsIfNeeded(userId);

        // 3. 重新获取积分信息（可能已被重置）
        creditModel = creditDao.findByUserId(userId);

        // 4. 获取订阅信息
        var subscription = subscriptionDao.findByUserId(userId);

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
        var creditModel = creditDao.findByUserId(userId);

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
}
