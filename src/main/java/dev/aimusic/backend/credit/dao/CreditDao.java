package dev.aimusic.backend.credit.dao;

import dev.aimusic.backend.subscription.dao.PlanType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditDao {

    private final CreditRepository creditRepository;

    public Optional<CreditModel> findByUserId(Long userId) {
        return creditRepository.findByUserId(userId);
    }

    public CreditModel save(CreditModel credit) {
        return creditRepository.save(credit);
    }

    public CreditModel createDefaultCredit(Long userId) {
        var credit = CreditModel.builder()
                .userId(userId)
                .permanentCredits(0)
                .renewableCredits(PlanType.FREE.getResetAmount()) // 新用户默认给FREE plan的积分
                .lastResetTime(LocalDateTime.now())
                .build();

        log.info("Creating default credit record for user: {}", userId);
        return save(credit);
    }
}
