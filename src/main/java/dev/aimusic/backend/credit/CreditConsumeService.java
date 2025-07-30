package dev.aimusic.backend.credit;

import com.google.common.annotations.VisibleForTesting;
import dev.aimusic.backend.common.exceptions.PaymentRequiredException;
import dev.aimusic.backend.credit.dao.CreditDao;
import dev.aimusic.backend.credit.dao.CreditModel;
import dev.aimusic.backend.credit_transcation.CreditLogger;
import dev.aimusic.backend.credit_transcation.dao.TriggerSource;
import dev.aimusic.backend.task.dao.TaskModel;
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
    private final CreditLogger creditLogger;

    /**
     * 消耗用户积分
     */
    public void consumeCredits(TaskModel task) {
        // 1. 再次检查积分是否足够（防止并发问题）
        checkCredits(task.getUserId(), task.getCreditsConsumed(), task.getTriggerSource());

        // 2. 获取积分记录
        var creditModel = creditDao.findByUserId(task.getUserId());

        // 3. 根据触发源执行消耗策略
        executeConsumptionStrategy(creditModel, task.getCreditsConsumed(), task.getTriggerSource());

        // 4. 保存更新后的积分
        creditDao.save(creditModel);

        // 5. 记录日志
        creditLogger.logCreditConsumption(task.getUserId(), task.getCreditsConsumed(), task.getTriggerSource(), task.getTaskId(),
                creditModel.getPermanentCredits(), creditModel.getRenewableCredits());
    }

    /**
     * 检查用户是否有足够积分，不足则抛出异常
     */
    @VisibleForTesting
    void checkCredits(Long userId, Integer requiredCredits, TriggerSource triggerSource) {
        var creditModel = creditDao.findByUserId(userId);

        // 2. 懒重置积分
        creditService.resetCreditsIfNeeded(userId);

        // 3. 重新获取积分信息（可能已被重置）
        creditModel = creditDao.findByUserId(userId);

        // 4. 计算可用积分
        var availableCredits = calculateAvailableCredits(creditModel, triggerSource);

        // 5. 检查是否足够
        if (availableCredits < requiredCredits) {
            var errorMessage = String.format("Insufficient credits. Required: %d, Available: %d",
                    requiredCredits, availableCredits);
            throw new PaymentRequiredException(errorMessage);
        }
    }

    @VisibleForTesting
    Integer calculateAvailableCredits(CreditModel creditModel, TriggerSource triggerSource) {
        return switch (triggerSource) {
            case UI -> creditModel.getRenewableCredits() + creditModel.getPermanentCredits();
            case API -> creditModel.getPermanentCredits(); // API只能使用永久积分
        };
    }

    @VisibleForTesting
    void executeConsumptionStrategy(CreditModel creditModel, Integer credits, TriggerSource triggerSource) {
        switch (triggerSource) {
            case UI -> consumeCreditsForUI(creditModel, credits);
            case API -> consumeCreditsForAPI(creditModel, credits);
        }
    }

    @VisibleForTesting
    void consumeCreditsForUI(CreditModel creditModel, Integer credits) {
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
                throw new PaymentRequiredException(
                        String.format("Insufficient permanent credits for UI consumption. Required: %d, Available: %d",
                                remainingCredits, creditModel.getPermanentCredits()));
            }
            creditModel.setPermanentCredits(creditModel.getPermanentCredits() - remainingCredits);
        }
    }

    @VisibleForTesting
    void consumeCreditsForAPI(CreditModel creditModel, Integer credits) {
        // API只能消耗永久积分
        if (creditModel.getPermanentCredits() < credits) {
            throw new PaymentRequiredException(
                    String.format("Insufficient permanent credits for API consumption. Required: %d, Available: %d",
                            credits, creditModel.getPermanentCredits()));
        }
        creditModel.setPermanentCredits(creditModel.getPermanentCredits() - credits);
    }
}
