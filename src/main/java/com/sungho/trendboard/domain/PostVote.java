package com.sungho.trendboard.domain;

import com.sungho.trendboard.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "post_votes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_votes_post_user",
                        columnNames = {"post_id", "user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostVote extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    private VoteType voteType;

    @Builder
    private PostVote(Long id, Long postId, Long userId, VoteType voteType) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.voteType = voteType;
    }


}
