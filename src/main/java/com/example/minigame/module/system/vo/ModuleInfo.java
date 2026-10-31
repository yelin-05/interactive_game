package com.example.minigame.module.system.vo;

/**
 * 首页功能模块条目（导航瓦片）。
 *
 * @param code   模块标识
 * @param name   模块名
 * @param icon   图标（emoji，避免引图标库）
 * @param desc   一句话说明
 * @param status 当前状态文案
 */
public record ModuleInfo(String code, String name, String icon, String desc, String status) {
}
