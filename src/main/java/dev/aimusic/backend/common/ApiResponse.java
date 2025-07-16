package dev.aimusic.backend.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private Integer code;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, 200);
    }

    public static <T> ApiResponse<T> error(String message, int code) {
        return new ApiResponse<>(false, null, message, code);
    }
}

