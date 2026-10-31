package com.example.minigame.module.task.bootstrap;

import com.example.minigame.module.task.entity.Task;
import com.example.minigame.module.task.mapper.TaskMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * 任务初始化：t_task 为空时灌入 resources/seed/tasks.json，幂等。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSeeder implements ApplicationRunner {

    private static final String SEED_FILE = "seed/tasks.json";

    private final TaskMapper taskMapper;
    private final ObjectMapper objectMapper;

    private record SeedTask(String title, String description, String category,
                            Integer targetCount, Integer rewardPoints) {
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Long existing = taskMapper.selectCount(null);
        if (existing != null && existing > 0) {
            log.info("任务已存在 {} 条，跳过初始化", existing);
            return;
        }
        ClassPathResource resource = new ClassPathResource(SEED_FILE);
        if (!resource.exists()) {
            log.warn("未找到任务种子文件 {}", SEED_FILE);
            return;
        }
        List<SeedTask> seeds;
        try (InputStream in = resource.getInputStream()) {
            seeds = objectMapper.readValue(in, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, SeedTask.class));
        }
        int inserted = 0;
        for (SeedTask s : seeds) {
            Task t = new Task();
            t.setTitle(s.title());
            t.setDescription(s.description());
            t.setCategory(s.category());
            t.setTargetCount(s.targetCount() == null ? 1 : s.targetCount());
            t.setRewardPoints(s.rewardPoints() == null ? 10 : s.rewardPoints());
            t.setStatus(1);
            taskMapper.insert(t);
            inserted++;
        }
        log.info("任务初始化完成，导入 {} 条", inserted);
    }
}
