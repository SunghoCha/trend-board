CREATE TABLE tag (
    id         BIGINT       NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tag_name UNIQUE (name)
) ENGINE = InnoDB;

DROP TABLE IF EXISTS post_tag;

CREATE TABLE post_tag (
    id      BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_post_tag_tag_id (tag_id),
    CONSTRAINT uk_post_tag_post_tag UNIQUE (post_id, tag_id),
    CONSTRAINT fk_post_tag_post FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id)
) ENGINE = InnoDB;

CREATE TABLE post_hashtag (
    id         BIGINT       NOT NULL,
    post_id    BIGINT       NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_post_hashtag_post_id (post_id),
    INDEX idx_post_hashtag_name (name),
    CONSTRAINT uk_post_hashtag_post_name UNIQUE (post_id, name),
    CONSTRAINT fk_post_hashtag_post FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE
) ENGINE = InnoDB;
