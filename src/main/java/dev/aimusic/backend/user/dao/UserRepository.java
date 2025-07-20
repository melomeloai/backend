package dev.aimusic.backend.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByClerkId(String clerkId);

    Optional<UserModel> findByEmail(String email);

    boolean existsByClerkId(String clerkId);
}

