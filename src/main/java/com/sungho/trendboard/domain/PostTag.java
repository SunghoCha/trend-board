package com.sungho.trendboard.domain;

import com.sungho.trendboard.global.util.SnowflakeId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "post_tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_tag_post_tag", columnNames = {"post_id", "tag_id"}),
        indexes = @Index(name = "idx_post_tag_tag_id", columnList = "tag_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag {

    @Id
    @SnowflakeId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    private PostTag(Post post, Tag tag) {
        this.post = post;
        this.tag = tag;
    }

    public static PostTag of(Post post, Tag tag) {
        return new PostTag(post, tag);
    }
}
