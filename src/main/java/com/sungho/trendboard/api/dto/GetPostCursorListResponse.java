package com.sungho.trendboard.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GetPostCursorListResponse(
        List<Item> items,
        int size,
        boolean hasNext,
        Long nextCursorId
) {
    public record Item(
            Long postId,
            Long authorId,
            String title,
            LocalDateTime createdAt
    ) {
    }

    public static GetPostCursorListResponse of(
            List<Item> items,
            int size,
            boolean hasNext,
            Long nextCursorId
    ) {
        return new GetPostCursorListResponse(items, size, hasNext, nextCursorId);
    }
}
