package com.example.minigame.module.system.controller;

import com.example.minigame.module.system.vo.ModuleInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 首页渲染。
 */
@Controller
public class HomeController {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    private static final List<ModuleInfo> MODULES = List.of(
            new ModuleInfo("task", "任务中心", "📋", "发布 · 领取 · 提交 · 状态流转", "骨架就绪"),
            new ModuleInfo("asset", "素材库", "🖼️", "上传 · 分类 · 检索", "骨架就绪"),
            new ModuleInfo("rank", "排行榜", "🏆", "积分 · 排名 · 分页", "骨架就绪"),
            new ModuleInfo("llm", "大模型生成", "🤖", "出题 · 文案 · Mock/真实", "骨架就绪")
    );

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("appVersion", appVersion);
        model.addAttribute("modules", MODULES);
        return "index";
    }
}
