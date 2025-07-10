package dev.aimusic.backend.credits.dao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditsRepository extends JpaRepository<CreditsModel, CreditsId> {
}