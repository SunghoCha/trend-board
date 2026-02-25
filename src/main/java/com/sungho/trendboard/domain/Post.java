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

    public void replacePostTags(List<Tag> tags) {
        this.postTags.clear();
        if (tags == null || tags.isEmpty()) {
            return;
        }
        tags.forEach(this::addPostTag);
    }

    public void replaceHashtags(List<String> hashtags) {
        this.postHashtags.clear();
        initializeHashtags(hashtags);
    }

    private void initializeHashtags(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return;
        }
        hashtags.forEach(this::addHashtag);
    }
}
