package dev.aimusic.backend.creditshistory.dao;

import dev.aimusic.backend.subscription.dao.UserPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "credits_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditsHistoryModel {

    @Id
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserPlan plan;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}