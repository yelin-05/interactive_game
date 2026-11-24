package com.example.moshi.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuizRoundTest {

    @Test
    void rejectsOutOfRangeAnswers() {
        QuizRound round = round(System.currentTimeMillis());
        assertThrows(IllegalArgumentException.class, () -> round.answer(4));
        assertThrows(IllegalArgumentException.class, () -> round.answer(-2));
    }

    @Test
    void lateAnswerIsRecordedAsUnanswered() {
        QuizRound round = round(System.currentTimeMillis() - 60_000L);
        round.answer(0);
        assertEquals(-1, round.getAnswers()[0]);
    }

    @Test
    void validAnswerIsScoredByServer() {
        QuizRound round = round(System.currentTimeMillis());
        round.answer(0);
        QuizRound.Result result = round.grade(false);
        assertEquals(1, result.getCorrect());
        assertEquals(1, result.getDetails().size());
        assertEquals(0, result.getDetails().get(0).getCorrectAnswer());
    }

    private QuizRound round(long startedAt) {
        return new QuizRound("history", QuizMode.EASY, List.of(
                new QuizRound.Item(1L, "题目", List.of("A", "B", "C", "D"), 1, 0, "解析")), startedAt);
    }
}
