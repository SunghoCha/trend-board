package com.sungho.trendboard.domain;

import com.sungho.trendboard.global.domain.BaseTimeEntity;
import com.sungho.trendboard.global.util.SnowflakeId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @SnowflakeId
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private PostCategory category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Post(Long memberId, String title, String content, PostCategory category, List<String> hashtags) {
        if (memberId == null) {
            throw new IllegalArgumentException("memberId는 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title은 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content는 필수입니다.");
        }
        if (category == null) {
            throw new IllegalArgumentException("category는 필수입니다.");
        }

        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.category = category;
        initializeHashtags(hashtags);
        this.likeCount = 0;
    }

    public List<String> getHashtags() {
        return postHashtags.stream()
                .map(PostHashtag::getName)
                .toList();
    }

    public void addPostTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag는 필수입니다.");
        }
        if (tag.getId() == null) {
            throw new IllegalArgumentException("tag.id는 필수입니다.");
        }
        boolean alreadyExists = postTags.stream()
                .anyMatch(postTag -> postTag.getTag().getId().equals(tag.getId()));
        if (alreadyExists) {
            return;
        }
        this.postTags.add(PostTag.of(this, tag));
    }

    /**
     * Adds a hashtag to the post if not already present.
     *
     * The provided hashtag is trimmed before being added; duplicate names (after trimming) are ignored.
     *
     * @param hashtag the hashtag text to add; leading and trailing whitespace will be removed
     * @throws IllegalArgumentException if {@code hashtag} is null or blank after trimming
     */
    public void addHashtag(String hashtag) {
        if (hashtag == null) {
            throw new IllegalArgumentException("hashtag는 필수입니다.");
        }
        String normalized = hashtag.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("hashtag는 필수입니다.");
        }
        boolean alreadyExists = postHashtags.stream()
                .map(PostHashtag::getName)
                .anyMatch(name -> name.equals(normalized));
        if (alreadyExists) {
            return;
        }
        this.postHashtags.add(PostHashtag.of(this, normalized));
    }

    /**
     * Update the post's title, content, and category.
     *
     * @param title    the new title; must not be null or blank
     * @param content  the new content; must not be null or blank
     * @param category the new category; must not be null
     * @throws IllegalArgumentException if `title` or `content` is null or blank, or if `category` is null
     */
    public void update(String title, String content, PostCategory category) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title은 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content는 필수입니다.");
        }
        if (category == null) {
            throw new IllegalArgumentException("category는 필수입니다.");
        }

        this.title = title;
        this.content = content;
        this.category = category;
    }

    /**
     * Replace the post's tag associations with the provided list of tags.
     *
     * @param tags the tags to associate with this post; if `null` or empty, all existing associations are removed and no new associations are added
     */
    public void replacePostTags(List<Tag> tags) {
        this.postTags.clear();
        if (tags == null || tags.isEmpty()) {
            return;
        }
        tags.forEach(this::addPostTag);
    }

    /**
     * Replaces the post's hashtags with the provided list.
     *
     * Clears all existing hashtags and adds each non-null, non-blank, trimmed hashtag from the given list.
     * Duplicate names are ignored; passing `null` or an empty list results in no hashtags.
     *
     * @param hashtags list of hashtag names to set on the post; may be null
     */
    public void replaceHashtags(List<String> hashtags) {
        this.postHashtags.clear();
        initializeHashtags(hashtags);
    }

    /**
     * Initialize the post's hashtags from the given list.
     *
     * Adds each hashtag string in the provided list to this post. If the list is null or empty,
     * no hashtags are added.
     *
     * @param hashtags list of hashtag strings; may be null or empty
     */
    private void initializeHashtags(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return;
        }
        hashtags.forEach(this::addHashtag);
    }
}
