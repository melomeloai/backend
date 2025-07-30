package dev.aimusic.backend.common.exceptions;

/**
 * 验证异常 - 对应HTTP 400
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}