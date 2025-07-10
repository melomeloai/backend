package dev.aimusic.backend.credits.dao;

import dev.aimusic.backend.subscription.dao.UserPlan;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class CreditsId implements Serializable {
    private String userId;
    private UserPlan plan;
}