-- =====================================================================
--  H2 内存库建表（演示 / 测试档）
--  每次启动重建，保证演示环境干净可复现
-- =====================================================================
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    nickname   VARCHAR(50),
    avatar     VARCHAR(255),
    role       VARCHAR(20)  NOT NULL DEFAULT 'PLAYER',
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_username UNIQUE (username)
);

DROP TABLE IF EXISTS t_question;

CREATE TABLE t_question
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    category     VARCHAR(32)   NOT NULL,
    question     VARCHAR(500)  NOT NULL,
    options_json VARCHAR(1000) NOT NULL,
    answer_index INT           NOT NULL DEFAULT 0,
    explanation  VARCHAR(1000),
    difficulty   TINYINT       NOT NULL DEFAULT 1,
    status       TINYINT       NOT NULL DEFAULT 1,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_question_category ON t_question (category);

DROP TABLE IF EXISTS t_score;

CREATE TABLE t_score
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    category      VARCHAR(32) NOT NULL,
    score         INT         NOT NULL,
    correct_count INT         NOT NULL,
    total_count   INT         NOT NULL,
    duration_ms   INT         NOT NULL,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_score_cat ON t_score (category, score, duration_ms);

CREATE INDEX idx_score_user ON t_score (user_id);

DROP TABLE IF EXISTS t_task;

CREATE TABLE t_task
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(100) NOT NULL,
    description   VARCHAR(300),
    category      VARCHAR(32),
    target_count  INT          NOT NULL,
    reward_points INT          NOT NULL,
    status        TINYINT      NOT NULL DEFAULT 1,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_status ON t_task (status);

DROP TABLE IF EXISTS t_user_task;

CREATE TABLE t_user_task
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT    NOT NULL,
    task_id       BIGINT    NOT NULL,
    reward_points INT       NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_task UNIQUE (user_id, task_id)
);

DROP TABLE IF EXISTS t_asset;

CREATE TABLE t_asset
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    original_name VARCHAR(255),
    content_type  VARCHAR(100),
    size_bytes    BIGINT,
    category      VARCHAR(32),
    stored_path   VARCHAR(300) NOT NULL,
    uploader_id   BIGINT,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_asset_category ON t_asset (category);
