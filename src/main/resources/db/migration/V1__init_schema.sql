-- V1: 初始表结构
-- 兼容 MySQL 8 与 H2(MODE=MySQL)，因此不写 ENGINE / CHARSET 子句，库级字符集在建库时指定。

CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE todos (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    completed   BOOLEAN      NOT NULL DEFAULT FALSE,
    due_date    DATETIME(6)  NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_todos_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 最高频查询：查某用户的未完成/已完成列表
CREATE INDEX idx_todos_user_completed ON todos (user_id, completed);
