package com.example.moshi.service;

import com.example.moshi.dao.QuestionDao;
import com.example.moshi.dao.ScoreDao;
import com.example.moshi.model.Question;
import com.example.moshi.model.QuizMode;
import com.example.moshi.model.QuizRound;
import com.example.moshi.model.Score;
import com.example.moshi.web.SessionKeys;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    public static final String ATTR_ROUND = "quizRound";

    public static final class Completion {
        private final QuizRound round;
        private final QuizRound.Result result;

        private Completion(QuizRound round, QuizRound.Result result) {
            this.round = round;
            this.result = result;
        }

        public QuizRound getRound() { return round; }
        public QuizRound.Result getResult() { return result; }
    }

    private final QuestionDao questions = new QuestionDao();
    private final ScoreDao scores = new ScoreDao();

    public QuizRound start(HttpSession session, String category, QuizMode mode) throws SQLException {
        List<Question> picked = questions.pick(category, mode, mode.getCount());
        if (picked.isEmpty()) {
            throw new IllegalArgumentException("这个分区在当前难度下还没有题目");
        }
        List<QuizRound.Item> items = new ArrayList<>();
        for (Question question : picked) {
            items.add(new QuizRound.Item(question.getId(), question.getQuestion(), question.getOptions(),
                    question.getDifficulty(), question.getAnswerIndex(), question.getExplanation()));
        }
        QuizRound round = new QuizRound(category, mode, items, System.currentTimeMillis());
        synchronized (session) {
            session.setAttribute(ATTR_ROUND, round);
        }
        return round;
    }

    public QuizRound getRound(HttpSession session) {
        synchronized (session) {
            QuizRound round = (QuizRound) session.getAttribute(ATTR_ROUND);
            if (round != null) {
                round.expire(System.currentTimeMillis());
            }
            return round;
        }
    }

    public Completion answer(HttpSession session, int pick) throws SQLException {
        synchronized (session) {
            QuizRound round = (QuizRound) session.getAttribute(ATTR_ROUND);
            if (round == null || round.isFinished()) {
                return null;
            }
            round.expire(System.currentTimeMillis());
            if (!round.isFinished()) {
                round.answer(pick);
            }
            if (!round.isFinished()) {
                return null;
            }
            return new Completion(round, finish(session, round));
        }
    }

    public Completion finishIfNeeded(HttpSession session) throws SQLException {
        synchronized (session) {
            QuizRound round = (QuizRound) session.getAttribute(ATTR_ROUND);
            if (round == null) {
                return null;
            }
            round.expire(System.currentTimeMillis());
            return round.isFinished() ? new Completion(round, finish(session, round)) : null;
        }
    }

    public void abandon(HttpSession session) {
        synchronized (session) {
            session.removeAttribute(ATTR_ROUND);
        }
    }

    private QuizRound.Result finish(HttpSession session, QuizRound round) throws SQLException {
        Long userId = (Long) session.getAttribute(SessionKeys.USER_ID);
        boolean saved = false;
        if (userId != null) {
            QuizRound.Result preview = round.grade(false);
            Score score = new Score();
            score.setUserId(userId);
            score.setCategory(round.getCategory());
            score.setScore(preview.getScore());
            score.setCorrectCount(preview.getCorrect());
            score.setTotalCount(preview.getTotal());
            score.setDurationMs(preview.getDurationMs());
            scores.insert(score);
            saved = true;
        }
        QuizRound.Result result = round.grade(saved);
        session.removeAttribute(ATTR_ROUND);
        return result;
    }
}
