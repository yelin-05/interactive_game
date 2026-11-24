package com.example.moshi.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LlmServiceTest {

    @Test
    void mockReturnsACompleteQuestionContract() throws Exception {
        System.setProperty("MOSHI_LLM_MODE", "mock");
        LlmService service = new LlmService();
        LlmService.Result result = service.generate("computer", "TCP");
        assertEquals("mock", result.getMode());
        assertEquals(4, result.getOptions().size());
        assertEquals(0, result.getAnswerIndex());
    }
}
