package com.sungho.trendboard.global.exception;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record ApiErrorResponse(
        int status,
        String code,
        String message,
        List<ValidationErrorItem> errors,
        String path,
        LocalDateTime timestamp
) {
    public static ApiErrorResponse fromErrorCode(ErrorCode errorCode,
                                                 List<ValidationErrorItem> errors,
                                                 String path) {
        return new ApiErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                errors == null ? Collections.emptyList() : errors,
                path,
                LocalDateTime.now()
        );
    }
}
