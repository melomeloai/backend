package dev.aimusic.backend.credit.dto;

import dev.aimusic.backend.common.dto.AbstractResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 积分信息响应DTO
 */
@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class CreditInfoResponse extends AbstractResponse {
    private int permanentCredits;        // 永久积分
    private int renewableCredits;        // 可重置积分
    private LocalDateTime nextResetTime; // 下次重置时间
}
