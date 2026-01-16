package com.sungho.trendboard.api.dto;

import com.sungho.trendboard.domain.Post;

public record CreatePostResponse(
        Long postId
) {
    public static CreatePostResponse of(Post post) {
        return new CreatePostResponse(post.getId());
    }
}
