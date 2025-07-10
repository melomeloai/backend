package dev.aimusic.backend.creditshistory.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditsHistoryDao {

    private final CreditsHistoryRepository creditsHistoryRepository;

    public CreditsHistoryModel save(CreditsHistoryModel creditTransaction) {
        return creditsHistoryRepository.save(creditTransaction);
    }
}
