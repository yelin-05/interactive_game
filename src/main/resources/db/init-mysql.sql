-- =====================================================================
--  建库脚本（MySQL 9.x / 8.x 通用），用 mysql 客户端执行一次即可：
--    mysql -u root -p < db/init-mysql.sql
--  字符集统一 utf8mb4，避免中文昵称、emoji 存不进去
-- =====================================================================
CREATE DATABASE IF NOT EXISTS `minigame`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 建议为应用单独建账号，不要用 root 跑业务
-- CREATE USER IF NOT EXISTS 'minigame'@'localhost' IDENTIFIED BY '换成你的密码';
-- GRANT ALL PRIVILEGES ON minigame.* TO 'minigame'@'localhost';
-- FLUSH PRIVILEGES;

USE `minigame`;
