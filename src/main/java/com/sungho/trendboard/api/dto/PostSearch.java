package com.sungho.trendboard.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostSearch {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private Integer page; // 1페이지 시작
    private Integer size;

    public PostSearch(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }

    public long offset() {
        return (long) (normalizedPage() - 1) * normalizedSize();
    }

    public int limit() {
        return normalizedSize();
    }

    public int normalizedPage() {
        if (page == null || page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public int normalizedSize() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            return MAX_SIZE;
        }
        return size;
    }

}
