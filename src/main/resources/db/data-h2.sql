-- =====================================================================
--  H2 演示数据
--  下面三个账号的密码摘要对应明文 password（仅演示用，后面做登录时再换）
-- =====================================================================
DELETE FROM t_user;

INSERT INTO t_user (username, password, nickname, avatar, role, status)
VALUES ('admin',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        '管理员', NULL, 'ADMIN', 1),
       ('player01',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        '玩家一号', NULL, 'PLAYER', 1),
       ('player02',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        '玩家二号', NULL, 'PLAYER', 1);
