package com.sungho.trendboard.global.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record OffsetPageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> OffsetPageResponse<T> from(Page<T> page) {
        return new OffsetPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
