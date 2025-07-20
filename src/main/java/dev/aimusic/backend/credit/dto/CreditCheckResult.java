package dev.aimusic.backend.credit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditCheckResult {
    private Boolean sufficient;
    private Integer permanentCredits;
    private Integer renewableCredits;
    private String planType;
    private String errorMessage;

    public static CreditCheckResult sufficient(Integer permanentCredits, Integer renewableCredits, String planType) {
        return CreditCheckResult.builder()
                .sufficient(true)
                .permanentCredits(permanentCredits)
                .renewableCredits(renewableCredits)
                .planType(planType)
                .build();
    }

    public static CreditCheckResult insufficient(Integer permanentCredits, Integer renewableCredits, String planType, String errorMessage) {
        return CreditCheckResult.builder()
                .sufficient(false)
                .permanentCredits(permanentCredits)
                .renewableCredits(renewableCredits)
                .planType(planType)
                .errorMessage(errorMessage)
                .build();
    }
}
