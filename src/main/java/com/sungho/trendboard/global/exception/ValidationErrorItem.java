package com.sungho.trendboard.global.exception;

public record ValidationErrorItem(
        String field,
        String reason
) {
}
