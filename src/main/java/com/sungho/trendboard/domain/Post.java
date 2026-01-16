package com.sungho.trendboard.domain;

import com.sungho.trendboard.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public Post(Long id, Long authorId, String title, String content) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(authorId, "authorId must not be null");
        Assert.hasText(title, "title must not be blank");
        Assert.hasText(content, "content must not be blank");

        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
    }
}
