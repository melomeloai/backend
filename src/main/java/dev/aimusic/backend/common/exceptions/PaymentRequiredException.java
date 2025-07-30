package dev.aimusic.backend.common.exceptions;

/**
 * 积分不足异常 - 需要付费或充值
 */
public class PaymentRequiredException extends RuntimeException {
    public PaymentRequiredException(String message) {
        super(message);
    }
}