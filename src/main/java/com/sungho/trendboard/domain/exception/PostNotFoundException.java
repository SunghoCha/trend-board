package com.sungho.trendboard.domain.exception;

import com.sungho.trendboard.global.exception.BusinessException;
import com.sungho.trendboard.global.exception.ErrorCode;

import java.util.Map;

public class PostNotFoundException extends BusinessException {

    private static final ErrorCode ERROR_CODE = PostErrorCode.POST_NOT_FOUND;

    public PostNotFoundException() {
        super(ERROR_CODE);
    }

    public PostNotFoundException(Long postId) {
        super(ERROR_CODE, Map.of("postId", postId));
    }
}
