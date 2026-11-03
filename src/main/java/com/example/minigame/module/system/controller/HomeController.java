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
            new ModuleInfo("task", "任务中心", "📋", "进度实时计算 · 完成后领取奖励", "已上线"),
            new ModuleInfo("asset", "素材库", "🖼️", "上传 · 按分区浏览", "已上线"),
            new ModuleInfo("rank", "排行榜", "🏆", "总榜 + 五分区榜 · 分数降序", "已上线"),
            new ModuleInfo("llm", "大模型生成", "🤖", "按分区 + 主题出题 · Mock 通道", "Mock 可用")
    );

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("appVersion", appVersion);
        model.addAttribute("modules", MODULES);
        return "index";
    }
}
