CREATE TABLE posts (
                       id BIGINT NOT NULL,
                       author_id BIGINT NOT NULL,
                       title VARCHAR(50) NOT NULL,
                       content TEXT NOT NULL,
                       up_count BIGINT NOT NULL DEFAULT 0,
                       down_count BIGINT NOT NULL DEFAULT 0,
                       created_at DATETIME(6) NOT NULL,
                       updated_at DATETIME(6) NOT NULL,
                       version BIGINT NULL,
                       PRIMARY KEY (id)
) ENGINE=InnoDB;