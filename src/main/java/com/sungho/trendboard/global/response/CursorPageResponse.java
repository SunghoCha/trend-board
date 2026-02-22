package com.sungho.trendboard.global.response;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> items,
        int size,
        boolean hasNext,
        Long nextCursorId
) {
    public static <T> CursorPageResponse<T> of(List<T> items, int requestedSize, Long nextCursorId) {
        boolean hasNext = items.size() == requestedSize;
        return new CursorPageResponse<>(items, items.size(), hasNext, hasNext ? nextCursorId : null);
    }
}
