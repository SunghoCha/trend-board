package com.sungho.trendboard.api.dto;

import com.sungho.trendboard.application.dto.PostDetail;

import java.time.LocalDateTime;

public record GetPostResponse(
        Long id,
        Long authorId,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isOwner
) {
    public static GetPostResponse of(PostDetail postDetail, boolean isOwner) {
        return new GetPostResponse(
                postDetail.id(),
                postDetail.authorId(),
                postDetail.title(),
                postDetail.content(),
                postDetail.createdAt(),
                postDetail.updatedAt(),
                isOwner
        );
    }
}
