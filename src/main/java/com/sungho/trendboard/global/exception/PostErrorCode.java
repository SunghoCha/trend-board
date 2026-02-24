package com.sungho.trendboard.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PostErrorCode implements ErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-NOT_FOUND", "게시글을 찾을 수 없습니다."),
    TAG_NOT_FOUND(HttpStatus.BAD_REQUEST, "POST-TAG_NOT_FOUND", "존재하지 않는 태그가 포함되어 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    PostErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
