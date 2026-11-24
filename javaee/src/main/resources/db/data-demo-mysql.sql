-- =====================================================================
--  本地演示账号（仅限本机教学演示，禁止用于公网部署）
--  密码摘要对应明文 password；正式部署请删除本文件并自行初始化管理员
-- =====================================================================
USE moshi;

INSERT IGNORE INTO t_user (username, password, nickname, avatar, role, status)
VALUES ('admin',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        '管理员', NULL, 'ADMIN', 1),
       ('player01',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        '玩家一号', NULL, 'PLAYER', 1),
       ('player02',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        '玩家二号', NULL, 'PLAYER', 1);
