package com.sungho.trendboard.domain;

import com.sungho.trendboard.global.domain.BaseTimeEntity;
import com.sungho.trendboard.global.util.SnowflakeId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "post_hashtag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostHashtag extends BaseTimeEntity {

    @Id
    @SnowflakeId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    private PostHashtag(Post post, String name) {
        this.post = post;
        this.name = name;
    }

    public static PostHashtag of(Post post, String name) {
        return new PostHashtag(post, name);
    }
}
