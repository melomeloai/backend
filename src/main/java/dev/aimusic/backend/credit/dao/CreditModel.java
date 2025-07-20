package dev.aimusic.backend.credit.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "credits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditModel {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "permanent_credits", nullable = false)
    @Builder.Default
    private Integer permanentCredits = 0;

    @Column(name = "renewable_credits", nullable = false)
    @Builder.Default
    private Integer renewableCredits = 0;

    @Column(name = "last_reset_time")
    private LocalDateTime lastResetTime;

    @Column(name = "next_reset_time")
    private LocalDateTime nextResetTime;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
