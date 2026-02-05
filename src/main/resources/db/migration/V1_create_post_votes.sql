CREATE TABLE post_votes
(
    id         BIGINT      NOT NULL,
    post_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    vote_type  VARCHAR(10) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_votes_post_user (post_id, user_id),
    CONSTRAINT chk_post_votes_vote_type CHECK (vote_type IN ('UP', 'DOWN'))
)