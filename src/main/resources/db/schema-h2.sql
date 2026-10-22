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
