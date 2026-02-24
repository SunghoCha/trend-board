package com.sungho.trendboard.application.dto;

import com.sungho.trendboard.domain.Post;

import java.time.LocalDateTime;

public record PostDetail(
        Long id,
        Long authorId,
        String title,
        String content,
        boolean owner,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDetail from(Post post, boolean owner) {
        return new PostDetail(
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
