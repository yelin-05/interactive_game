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
            new ModuleInfo("task", "任务中心", "发布、领取、提交任务，带状态流转", "骨架就绪"),
            new ModuleInfo("asset", "素材库", "图片/音频/文本素材的上传与检索", "骨架就绪"),
            new ModuleInfo("rank", "排行榜", "按积分与通关时间排名，分页展示", "骨架就绪"),
            new ModuleInfo("llm", "大模型生成", "生成占位文案与素材，Mock/真实双通道", "骨架就绪")
    );

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("appVersion", appVersion);
        model.addAttribute("modules", MODULES);
        return "index";
    }
}
