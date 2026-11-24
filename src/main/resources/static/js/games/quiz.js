/**
 * 知识闯关 · 前端
 *
 * 判分完全在服务端，但改为「即时反馈」：
 *   1) GET  /api/quiz/modes        → 难度档
 *   2) GET  /api/quiz/categories   → 分区列表
 *   3) POST /api/quiz/start        → roundId + 题目（不含答案）
 *   4) 每答一题 POST /api/quiz/answer → 立即返回对错/连击/本题得分
 *   5) 全部答完 POST /api/quiz/submit → 服务端结算、入库、返回逐题解析
 */
(function () {
    'use strict';

    const elPick = document.getElementById('view-pick');
    const elPlay = document.getElementById('view-play');
    const elGrid = document.getElementById('pick-grid');
    const elModeGrid = document.getElementById('mode-grid');
    const elPickTip = document.getElementById('pick-tip');

    const elProgress = document.getElementById('hud-progress');
    const elTime = document.getElementById('hud-time');
    const elAnswered = document.getElementById('hud-answered');
    const elCombo = document.getElementById('hud-combo');
    const elScore = document.getElementById('hud-score');
    const elCat = document.getElementById('play-cat');
    const elIdx = document.getElementById('play-idx');
    const elDots = document.getElementById('dots');
    const elBar = document.getElementById('q-bar');
    const elMeta = document.getElementById('q-meta');
    const elText = document.getElementById('q-text');
    const elOpts = document.getElementById('opts');
    const elFeedback = document.getElementById('feedback');
    const elVerdict = document.getElementById('fb-verdict');
    const elGain = document.getElementById('fb-gain');
    const elFbCombo = document.getElementById('fb-combo');
    const elFbExplain = document.getElementById('fb-explain');
    const btnNext = document.getElementById('btn-next');
    const btnQuit = document.getElementById('btn-quit');
    const btnAgain = document.getElementById('btn-again');
    const btnBack = document.getElementById('btn-back');
    const modal = document.getElementById('modal');

    const state = {
        modes: [],
        categories: [],
        mode: null,
        current: null,
        roundId: null,
        questions: [],
        answers: [],
        index: -1,
        secondsPerQuestion: 15,
        remain: 15,
        answered: false,
        combo: 0,
        score: 0,
        tickId: null,
        submitting: false
    };

    const DIFF_NAME = {1: '简单', 2: '中等', 3: '困难'};

    /* ---------------- HTTP ---------------- */

    function request(path, options) {
        const opts = options || {};
        opts.headers = Object.assign({'Accept': 'application/json'}, opts.headers || {});
        return fetch(path, opts).then(function (resp) {
            return resp.json().then(function (json) {
                if (!resp.ok || json.code !== 0) {
                    throw new Error(json.message || ('HTTP ' + resp.status));
                }
                return json.data;
            });
        });
    }

    function stopTick() {
        if (state.tickId) {
            clearInterval(state.tickId);
            state.tickId = null;
        }
    }

    function letter(i) {
        return String.fromCharCode(65 + i);
    }

    /* ---------------- 选难度 + 分区 ---------------- */

    function loadConfig() {
        return Promise.all([
            request('/api/quiz/modes'),
            request('/api/quiz/categories')
        ]).then(function (results) {
            state.modes = results[0];
            state.categories = results[1];
            renderModes();
            renderCategories();
            const total = state.categories.reduce(function (n, c) { return n + c.count; }, 0);
            elPickTip.textContent = '共 ' + total + ' 道题，分 ' + state.categories.length +
                ' 个区。登录后成绩会自动入库并计入排行榜。';
        }).catch(function (e) {
            elModeGrid.innerHTML = '';
            elGrid.innerHTML = '';
            elPickTip.textContent = '加载失败：' + e.message;
        });
    }

    function renderModes() {
        elModeGrid.innerHTML = '';
        state.modes.forEach(function (m) {
            const card = document.createElement('button');
            card.type = 'button';
            card.className = 'mode-card';
            card.dataset.code = m.code;
            card.innerHTML =
                '<span class="mode-icon"></span>' +
                '<span class="mode-name"></span>' +
                '<span class="mode-desc"></span>';
            card.querySelector('.mode-icon').textContent = m.icon;
            card.querySelector('.mode-name').textContent = m.name;
            card.querySelector('.mode-desc').textContent = m.desc;
            card.addEventListener('click', function () { selectMode(m.code); });
            elModeGrid.appendChild(card);
        });
        const normal = state.modes.find(function (m) { return m.code === 'normal'; });
        selectMode(normal ? normal.code : (state.modes[0] && state.modes[0].code));
    }

    function selectMode(code) {
        state.mode = code;
        Array.prototype.forEach.call(elModeGrid.children, function (c) {
            c.classList.toggle('on', c.dataset.code === code);
        });
    }

    function renderCategories() {
        elGrid.innerHTML = '';
        state.categories.forEach(function (c) {
            const card = document.createElement('button');
            card.type = 'button';
            card.className = 'pick-card';
            card.innerHTML =
                '<span class="pick-icon"></span>' +
                '<span class="pick-name"></span>' +
                '<span class="pick-desc"></span>' +
                '<span class="pick-count"></span>';
            card.querySelector('.pick-icon').textContent = c.icon;
            card.querySelector('.pick-name').textContent = c.name;
            card.querySelector('.pick-desc').textContent = c.desc;
            card.querySelector('.pick-count').textContent = c.count + ' 题';
            card.addEventListener('click', function () { startGame(c); });
            elGrid.appendChild(card);
        });
    }

    /* ---------------- 开局 ---------------- */

    function startGame(category) {
        if (!state.mode) { selectMode('normal'); }
        state.current = category;
        modal.hidden = true;
        elPick.hidden = true;
        elPlay.hidden = false;
        elFeedback.hidden = true;
        elText.textContent = '正在抽题…';
        elOpts.innerHTML = '';
        elDots.innerHTML = '';

        request('/api/quiz/start?category=' + encodeURIComponent(category.code) +
                '&mode=' + encodeURIComponent(state.mode), {method: 'POST'})
            .then(function (data) {
                if (!data.questions || !data.questions.length) {
                    elText.textContent = '这个难度下该分区还没有题目，换个难度或分区试试。';
                    return;
                }
                state.roundId = data.roundId;
                state.questions = data.questions;
                state.secondsPerQuestion = data.secondsPerQuestion || 15;
                state.answers = new Array(data.questions.length).fill(-1);
                state.index = -1;
                state.combo = 0;
                state.score = 0;

                const cat = state.current;
                elCat.textContent = cat.icon + ' ' + cat.name + ' · ' + (data.modeName || '');
                elDots.innerHTML = '';
                data.questions.forEach(function () {
                    const d = document.createElement('i');
                    d.className = 'dot';
                    elDots.appendChild(d);
                });

                renderHud();
                nextQuestion();
            })
            .catch(function (e) {
                elText.textContent = '开局失败：' + e.message;
            });
    }

    function renderHud() {
        const total = state.questions.length || 1;
        elProgress.textContent = Math.max(1, Math.min(state.index + 1, total)) + '/' + total;
        elAnswered.textContent = String(state.answers.filter(function (a) { return a >= 0; }).length);
        elCombo.textContent = String(state.combo);
        elScore.textContent = String(state.score);
        elTime.textContent = state.answered ? '--' : String(state.remain);
        elTime.className = state.answered ? '' : (state.remain <= 5 ? 'bad' : (state.remain <= 8 ? 'warn' : ''));
        elIdx.textContent = '第 ' + Math.max(0, state.index + 1) + ' / ' + state.questions.length + ' 题';
        elBar.style.width = (state.answered ? 0 : (state.remain / state.secondsPerQuestion) * 100) + '%';

        const dots = elDots.children;
        for (let i = 0; i < dots.length; i++) {
            dots[i].classList.toggle('now', i === state.index && !state.answered);
        }
    }

    function nextQuestion() {
        state.index++;
        if (state.index >= state.questions.length) {
            submit();
            return;
        }
        const q = state.questions[state.index];
        state.remain = state.secondsPerQuestion;
        state.answered = false;

        elFeedback.hidden = true;
        elText.textContent = q.question;
        elMeta.textContent = '难度：' + (DIFF_NAME[q.difficulty] || '未知') +
            ' · 本题 ' + (q.baseScore || 0) + ' 分';
        elOpts.innerHTML = '';

        q.options.forEach(function (text, i) {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'opt';
            btn.innerHTML = '<span class="opt-key"></span><span class="opt-text"></span>';
            btn.querySelector('.opt-key').textContent = letter(i);
            btn.querySelector('.opt-text').textContent = text;
            btn.addEventListener('click', function () { choose(i); });
            elOpts.appendChild(btn);
        });

        renderHud();
        stopTick();
        state.tickId = setInterval(function () {
            state.remain--;
            if (state.remain <= 0) {
                state.remain = 0;
                renderHud();
                choose(-1);
                return;
            }
            renderHud();
        }, 1000);
    }

    function choose(picked) {
        if (state.answered) return;
        state.answered = true;
        stopTick();
        state.answers[state.index] = picked;

        Array.prototype.forEach.call(elOpts.children, function (b) {
            b.disabled = true;
        });

        request('/api/quiz/answer', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({roundId: state.roundId, index: state.index, answer: picked})
        }).then(function (res) {
            state.combo = res.combo;
            state.score += res.gained;
            showFeedback(res, picked);
        }).catch(function (e) {
            elVerdict.textContent = '⚠ 作答失败';
            elVerdict.className = 'fb-verdict';
            elGain.textContent = '';
            elFbCombo.textContent = '';
            elFbExplain.textContent = '提交失败：' + e.message + '，可继续下一题。';
            elFeedback.hidden = false;
            renderHud();
        });
    }

    function showFeedback(res, picked) {
        Array.prototype.forEach.call(elOpts.children, function (b, i) {
            if (i === res.correctAnswer) b.classList.add('correct');
            if (i === picked && !res.correct) b.classList.add('wrong');
            if (i === picked) b.classList.add('picked');
        });

        const dot = elDots.children[state.index];
        if (dot) dot.classList.add(res.correct ? 'ok' : 'bad');

        elVerdict.textContent = res.correct ? '✓ 答对了' : '✗ 答错了';
        elVerdict.className = 'fb-verdict ' + (res.correct ? 'ok' : 'bad');
        elGain.textContent = (res.gained >= 0 ? '+' : '') + res.gained;
        elGain.className = 'fb-gain ' + (res.gained >= 0 ? 'ok' : 'bad');

        if (res.correct) {
            elFbCombo.textContent = res.combo > 1
                ? '连击 ×' + res.combo + '，额外 +' + ((res.combo - 1) * 2) + ' 分'
                : '保持连击，继续冲刺！';
        } else {
            elFbCombo.textContent = picked < 0 ? '超时未作答，连击清零，-5 分' : '连击清零，-5 分';
        }

        elFbExplain.textContent = '正确答案：' + letter(res.correctAnswer) +
            (res.explanation ? '　—　' + res.explanation : '');

        elFeedback.hidden = false;
        renderHud();
        btnNext.focus();
    }

    /* ---------------- 交卷 ---------------- */

    function submit() {
        if (state.submitting) return;
        state.submitting = true;
        stopTick();
        elFeedback.hidden = true;
        elText.textContent = '正在结算…';
        elOpts.innerHTML = '';

        request('/api/quiz/submit', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({roundId: state.roundId, answers: state.answers})
        }).then(function (res) {
            showResult(res);
        }).catch(function (e) {
            elText.textContent = '结算失败：' + e.message;
        }).finally(function () {
            state.submitting = false;
        });
    }

    function showResult(res) {
        document.getElementById('r-score').textContent = String(res.score);
        document.getElementById('r-right').textContent = res.correct + ' / ' + res.total;
        document.getElementById('r-maxcombo').textContent = '×' + res.maxCombo;
        document.getElementById('r-breakdown').textContent =
            '基础 ' + res.baseScore + ' + 连击 ' + res.comboBonus +
            ' − 失误 ' + res.penalty + ' + 速度 ' + res.speedBonus;
        document.getElementById('r-time').textContent = Math.round(res.durationMs / 1000) + 's';
        document.getElementById('r-saved').textContent = res.saved ? '已入库' : '未登录，未保存';
        document.getElementById('r-sub').textContent =
            res.saved ? '本局得分（已计入排行榜）' : '本局得分（登录后才会入库）';

        const box = document.getElementById('r-details');
        box.innerHTML = '';
        res.details.forEach(function (d, i) {
            const row = document.createElement('div');
            row.className = 'detail ' + (d.correct ? 'ok' : 'bad');
            const head = document.createElement('div');
            head.className = 'detail-head';
            head.textContent = (d.correct ? '✓ ' : '✗ ') + (i + 1) + '. ' + d.question +
                '  [' + (DIFF_NAME[d.difficulty] || '') + ']';
            const body = document.createElement('div');
            body.className = 'detail-body';
            const yours = d.yourAnswer >= 0 ? letter(d.yourAnswer) : '未作答';
            const gain = (d.gained >= 0 ? '+' : '') + d.gained;
            body.textContent = '你的选择：' + yours + '　正确答案：' + letter(d.correctAnswer) +
                '　得分：' + gain +
                (d.explanation ? '　—　' + d.explanation : '');
            row.appendChild(head);
            row.appendChild(body);
            box.appendChild(row);
        });

        const tip = document.getElementById('r-tip');
        if (res.saved) {
            tip.textContent = '成绩已保存，去排行榜看看排第几。';
        } else {
            tip.innerHTML = '未登录，成绩没有保存。<a class="inline-link" href="/login">登录</a>后再玩就能上榜。';
        }

        modal.hidden = false;
    }

    /* ---------------- 事件 ---------------- */

    btnNext.addEventListener('click', function () {
        if (state.answered) nextQuestion();
    });

    btnQuit.addEventListener('click', function () {
        stopTick();
        elPlay.hidden = true;
        elPick.hidden = false;
    });

    btnAgain.addEventListener('click', function () {
        if (state.current) startGame(state.current);
    });

    btnBack.addEventListener('click', function () {
        stopTick();
        modal.hidden = true;
        elPlay.hidden = true;
        elPick.hidden = false;
    });

    document.addEventListener('keydown', function (e) {
        if (elPlay.hidden) return;
        const key = e.key.toUpperCase();
        if (!state.answered) {
            let pick = -1;
            if (key >= '1' && key <= '9') pick = Number(key) - 1;
            else if (key >= 'A' && key <= 'D') pick = key.charCodeAt(0) - 65;
            if (pick >= 0 && pick < elOpts.children.length) {
                e.preventDefault();
                choose(pick);
            }
        } else if (key === 'ENTER') {
            e.preventDefault();
            nextQuestion();
        }
    });

    loadConfig();
})();
