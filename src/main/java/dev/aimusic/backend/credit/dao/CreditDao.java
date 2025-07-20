package dev.aimusic.backend.credit.dao;

import dev.aimusic.backend.common.exceptions.NotFoundException;
import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditDao {

    private final CreditRepository creditRepository;

    public CreditModel findByUserId(Long userId) {
        return creditRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Credit record not found for user ID: " + userId));
    }

    public CreditModel save(CreditModel credit) {
        return creditRepository.save(credit);
    }

    public void createDefaultCredit(Long userId) {
        log.info("Creating default credit record for user: {}", userId);
        save(CreditModel.builder()
                .userId(userId)
                .permanentCredits(0)
                .renewableCredits(PlanType.FREE.getResetAmount()) // 新用户默认给FREE plan的积分
                .lastResetTime(LocalDateTime.now())
                .nextResetTime(LocalDateTime.now().plusDays(1)) // FREE plan每日重置
                .build());
    }
}
