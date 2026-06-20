package com.taskforge.common.exception;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        String path
) {
    public ApiError(int status, String error, String message, String path) {
        this(status, error, message, LocalDateTime.now(), path);
    }
}
