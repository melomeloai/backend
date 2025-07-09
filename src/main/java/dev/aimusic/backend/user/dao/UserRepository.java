package dev.aimusic.backend.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, String> {
    Optional<UserModel> findByGoogleSub(String googleSub);
}

