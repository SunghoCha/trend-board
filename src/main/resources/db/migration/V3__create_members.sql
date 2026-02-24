CREATE TABLE member (
    id         BIGINT       NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    nickname   VARCHAR(50)  NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    point      INT          NOT NULL DEFAULT 0,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_email (email)
) ENGINE = InnoDB;
