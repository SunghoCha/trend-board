package com.sungho.trendboard.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GetPostListResponse(
    List<Item> items,
    int page,
    int size,
    boolean hasNext
) {
    public record Item(
        Long postId,
        Long authorId,
        String title,
        LocalDateTime createdAt
    ) {}

    public static GetPostListResponse of(List<Item> items, int page, int size, boolean hasNext) {
        return new GetPostListResponse(items, page, size, hasNext);
    }
}
