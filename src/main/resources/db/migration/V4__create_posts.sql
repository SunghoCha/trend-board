CREATE TABLE post (
    id         BIGINT       NOT NULL,
    member_id  BIGINT       NOT NULL,
    title      VARCHAR(50)  NOT NULL,
    content    TEXT         NOT NULL,
    category   VARCHAR(30)  NOT NULL,
    tags       VARCHAR(255) NULL,
    like_count INT          NOT NULL DEFAULT 0,
    deleted_at DATETIME(6)  NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_post_member_id (member_id),
    INDEX idx_post_category (category),
    CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB;
