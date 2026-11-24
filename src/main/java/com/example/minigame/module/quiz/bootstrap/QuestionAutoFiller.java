package com.example.minigame.module.quiz.bootstrap;

import com.example.minigame.module.quiz.service.QuestionAutoFillService;
import com.example.minigame.module.quiz.vo.AutoFillReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 自动补题的触发入口：启动时可选跑一次 + 定时任务（都靠配置开关，默认不打扰）。
 *
 * <p>三个触发点：
 * <ul>
 *   <li>启动时：<code>app.quiz.auto-fill.on-startup=true</code>（默认 false，避免第一次部署就卡在联网出题上）；</li>
 *   <li>定时：<code>app.quiz.auto-fill.cron</code>（默认 <code>-</code> 表示关闭）；</li>
 *   <li>后台按钮：<code>POST /admin/questions/autofill</code>，演示时点一下就看到结果。</li>
 * </ul>
 *
 * <p>{@code @Order(20)}：必须排在 {@link QuestionSeeder}（10）之后，
 * 否则种子题还没灌完就去算缺口，会把已经存在的题重复生成一遍。
 */
@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.quiz.auto-fill.enabled", havingValue = "true", matchIfMissing = true)
public class QuestionAutoFiller implements ApplicationRunner {

    private final QuestionAutoFillService autoFillService;

    @Value("${app.quiz.auto-fill.on-startup:false}")
    private boolean onStartup;

    @Override
    public void run(ApplicationArguments args) {
        if (!onStartup) {
            log.debug("自动补题：on-startup=false，启动阶段跳过（可在后台点「让游戏自己补题」）");
            return;
        }
        runOnce("启动");
    }

    @Scheduled(cron = "${app.quiz.auto-fill.cron:-}")
    public void scheduled() {
        runOnce("定时");
    }

    /** 补题失败绝不能影响启动/请求，所以在这里兜住所有异常。 */
    private void runOnce(String trigger) {
        try {
            AutoFillReportVO report = autoFillService.fillAll();
            log.info("自动补题（{}触发）结束：新增 {} 道，拒收 {} 道，通道={}",
                    trigger, report.inserted(), report.rejected(), report.mode());
        } catch (Exception e) {
            log.warn("自动补题（{}触发）失败，不影响主流程：{}", trigger, e.getMessage());
        }
    }
}
