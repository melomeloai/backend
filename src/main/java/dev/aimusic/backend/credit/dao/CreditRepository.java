package dev.aimusic.backend.credit.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditRepository extends JpaRepository<CreditModel, Long> {
    Optional<CreditModel> findByUserId(Long userId);
}