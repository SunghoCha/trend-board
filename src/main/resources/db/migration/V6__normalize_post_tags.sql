CREATE TABLE post_tag (
    post_id BIGINT      NOT NULL,
    tag     VARCHAR(50) NOT NULL,
    PRIMARY KEY (post_id, tag),
    INDEX idx_post_tag_tag (tag),
    CONSTRAINT fk_post_tag_post FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE
) ENGINE = InnoDB;

ALTER TABLE post
    DROP COLUMN tags;
