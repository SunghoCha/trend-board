package com.sungho.trendboard.application.dto;

import com.sungho.trendboard.domain.Post;

import java.time.LocalDateTime;

public record PostDetail(
        Long id,
        Long authorId,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDetail from(Post post) {
        return new PostDetail(
                post.getId(),
                post.getAuthorId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
