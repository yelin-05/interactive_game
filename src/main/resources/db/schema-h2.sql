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
