package com.sungho.trendboard.api.dto;

import com.sungho.trendboard.application.dto.PostDetail;
import com.sungho.trendboard.domain.Post;

import java.time.LocalDateTime;

public record GetPostResponse(
        Long id,
        Long authorId,
        String title,
        String content,
        boolean owner,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GetPostResponse of(Post post, boolean owner) {
        return new GetPostResponse(
                post.getId(),
                post.getAuthorId(),
                post.getTitle(),
                post.getContent(),
                owner,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
