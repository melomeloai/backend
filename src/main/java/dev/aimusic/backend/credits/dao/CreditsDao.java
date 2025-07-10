package dev.aimusic.backend.credits.dao;

import dev.aimusic.backend.subscription.dao.UserPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditsDao {

    private final CreditsRepository creditsRepository;

    public CreditsModel getCredits(String userId, UserPlan plan) {
        var key = CreditsId.builder()
                .userId(userId)
                .plan(plan)
                .build();
        return creditsRepository.findById(key).orElse(null);
    }

    public CreditsModel save(CreditsModel creditsModel) {
        return creditsRepository.save(creditsModel);
    }
}
