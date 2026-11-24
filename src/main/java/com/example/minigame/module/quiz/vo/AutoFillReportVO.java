package com.example.minigame.module.quiz.vo;

import java.util.List;

/**
 * 自动补题结果（后台页面与日志共用）。
 *
 * @param mode     本次实际用的通道：real（真实模型）/ mock（占位）
 * @param inserted 新入库题数
 * @param rejected 校验没通过、被丢弃的题数
 * @param notes    每个分区一行：现有题量、补了多少、拒收原因
 */
public record AutoFillReportVO(
        String mode,
        int inserted,
        int rejected,
        List<String> notes
) {
}
