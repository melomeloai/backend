package dev.aimusic.backend.common.exceptions;

/**
 * 认证异常 - JWT token无效、过期或缺失
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}