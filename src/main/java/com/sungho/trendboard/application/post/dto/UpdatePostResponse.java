package com.sungho.trendboard.application.post.dto;

import com.sungho.trendboard.domain.Post;
import com.sungho.trendboard.domain.PostCategory;
import com.sungho.trendboard.domain.PostHashtag;

import java.time.LocalDateTime;
import java.util.List;

public record UpdatePostResponse(
        Long id,
        Long memberId,
        String title,
        String content,
        PostCategory category,
        List<Long> tagIds,
        List<String> hashtags,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Create an UpdatePostResponse DTO from a Post entity.
     *
     * @param post the source Post entity to extract values from
     * @return an UpdatePostResponse containing the post's id, memberId, title, content, category,
     *         list of tag IDs, list of hashtag names, likeCount, createdAt, and updatedAt
     */
    public static UpdatePostResponse from(Post post) {
        return new UpdatePostResponse(
                post.getId(),
                post.getMemberId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getPostTags().stream()
                        .map(postTag -> postTag.getTag().getId())
                        .toList(),
                post.getPostHashtags().stream()
                        .map(PostHashtag::getName)
                        .toList(),
                post.getLikeCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
