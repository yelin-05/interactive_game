package com.example.minigame;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 互动 Web 小游戏 —— 任务与素材管理平台 启动类。
 *
 * <p>模块规划：任务 / 素材库 / 排行榜 / 大模型生成占位。
 */
@SpringBootApplication
@MapperScan("com.example.minigame.module.**.mapper")
public class MinigameApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinigameApplication.class, args);
    }
}
