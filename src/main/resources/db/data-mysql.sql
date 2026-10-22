-- =====================================================================
--  MySQL 演示数据
--  用 INSERT IGNORE + username 唯一键，重复启动不会重复插入，也不会报错
--  密码摘要对应明文 password（仅演示用，做登录模块时会换成真实注册流程）
-- =====================================================================
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
