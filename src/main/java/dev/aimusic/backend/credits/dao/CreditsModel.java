package dev.aimusic.backend.credits.dao;

import dev.aimusic.backend.subscription.dao.UserPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_credits")
@IdClass(CreditsId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditsModel {
    @Id
    @Column(nullable = false, updatable = false)
    private String userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private UserPlan plan;

    @Column(nullable = false)
    private int credits;

    private OffsetDateTime lastResetAt;

    private OffsetDateTime nextResetAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}

