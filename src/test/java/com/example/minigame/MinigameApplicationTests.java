package com.example.minigame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 骨架冒烟测试：跑在 H2 内存档上，验证「应用能起、接口能通、库能连」。
 * CI 上不需要 MySQL 也能过。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class MinigameApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Spring 容器正常启动")
    void contextLoads() {
        // 上下文能加载说明 Bean、数据源、Mapper 扫描都没问题
    }

    @Test
    @DisplayName("GET /api/ping 返回统一结构且数据库可用")
    void pingReturnsOk() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.dbStatus").value("UP"))
                .andExpect(jsonPath("$.data.userCount").isNumber());
    }

    @Test
    @DisplayName("GET / 首页渲染成功")
    void indexRenders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
