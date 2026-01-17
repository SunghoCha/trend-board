package com.sungho.trendboard.application.dto;

import java.time.LocalDateTime;

public record  PostListItem (
        Long postId,
        Long authorId,
        String title,
        LocalDateTime createdAt
){
}
