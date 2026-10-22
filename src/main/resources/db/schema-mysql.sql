-- =====================================================================
--  MySQL 建表（正式 / 评分档）
--  全部 IF NOT EXISTS：重复启动不会清掉已有数据
--  字符集 utf8mb4，排序规则统一，中文与 emoji 都能存
-- =====================================================================

CREATE TABLE IF NOT EXISTS t_user
(
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username   VARCHAR(50)  NOT NULL COMMENT '登录名，唯一',
    password   VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码摘要，禁止明文',
    nickname   VARCHAR(50)  DEFAULT NULL COMMENT '昵称，排行榜显示',
    avatar     VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    role       VARCHAR(20)  NOT NULL DEFAULT 'PLAYER' COMMENT 'PLAYER / ADMIN',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 0 禁用',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户表';
