package dev.aimusic.backend.credit.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分信息响应DTO
 */
@Builder
@Data
public class CreditInfoResponse {
    private int permanentCredits;        // 永久积分
    private int renewableCredits;        // 可重置积分
    private LocalDateTime nextResetTime; // 下次重置时间
    private String planType;             // 当前订阅计划
}
