/**
 * 知识闯关 · 前端
 *
 * 判分完全在服务端：
 *   1) POST /api/quiz/start?category=xxx  → 拿到 roundId + 题目（**没有答案**）
 *   2) 用户在本地作答，只记录「每题选了哪个」
 *   3) POST /api/quiz/submit {roundId, answers} → 服务端比对、算分、入库，返回逐题解析
 *
 * 因此答题过程中不显示对错（像考试），交卷后才一次性给结果。
 */
(function () {
    'use strict';

    const elPick = document.getElementById('view-pick');
    const elPlay = document.getElementById('view-play');
    const elGrid = document.getElementById('pick-grid');
    const elPickTip = document.getElementById('pick-tip');

    const elProgress = document.getElementById('hud-progress');
    const elTime = document.getElementById('hud-time');
    const elAnswered = document.getElementById('hud-answered');
    const elCat = document.getElementById('play-cat');
    const elIdx = document.getElementById('play-idx');
    const elDots = document.getElementById('dots');
    const elBar = document.getElementById('q-bar');
    const elText = document.getElementById('q-text');
    const elOpts = document.getElementById('opts');
    const elExplain = document.getElementById('explain');
    const btnQuit = document.getElementById('btn-quit');
    const btnAgain = document.getElementById('btn-again');
    const btnBack = document.getElementById('btn-back');
    const modal = document.getElementById('modal');

    const state = {
        categories: [],
        current: null,
        roundId: null,
        questions: [],
        answers: [],
        index: -1,
        secondsPerQuestion: 15,
        remain: 15,
        answered: false,
        tickId: null,
        submitting: false
    };

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

    /* ---------------- 选分区 ---------------- */

    function loadCategories() {
        return request('/api/quiz/categories').then(function (list) {
            state.categories = list;
            renderCategories();
            elPickTip.textContent = '共 ' + list.reduce(function (n, c) {
                return n + c.count;
            }, 0) + ' 道题，分 ' + list.length + ' 个区。登录后成绩会自动入库并计入排行榜。';

            const wanted = new URLSearchParams(location.search).get('category');
            if (wanted) {
                const hit = list.find(function (c) {
                    return c.code === wanted;
                });
                if (hit) startGame(hit);
            }
        }).catch(function (e) {
            elGrid.innerHTML = '';
            elPickTip.textContent = '题库加载失败：' + e.message;
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
            card.addEventListener('click', function () {
                startGame(c);
            });
            elGrid.appendChild(card);
        });
    }

    /* ---------------- 开局 ---------------- */

    function startGame(category) {
        modal.hidden = true;
        elPick.hidden = true;
        elPlay.hidden = false;
        elText.textContent = '正在抽题…';
        elOpts.innerHTML = '';
        elExplain.hidden = true;
        elDots.innerHTML = '';

        request('/api/quiz/start?category=' + encodeURIComponent(category.code), {method: 'POST'})
            .then(function (data) {
                if (!data.questions || !data.questions.length) {
                    elText.textContent = '这个分区还没有题目。';
                    return;
                }
                state.current = category;
                state.roundId = data.roundId;
                state.questions = data.questions;
                state.secondsPerQuestion = data.secondsPerQuestion || 15;
                state.answers = new Array(data.questions.length).fill(-1);
                state.index = -1;

                elCat.textContent = category.icon + ' ' + category.name;
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

        elText.textContent = q.question;
        elExplain.hidden = true;
        elExplain.textContent = '';
        elOpts.innerHTML = '';

        q.options.forEach(function (text, i) {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'opt';
            btn.innerHTML = '<span class="opt-key"></span><span class="opt-text"></span>';
            btn.querySelector('.opt-key').textContent = letter(i);
            btn.querySelector('.opt-text').textContent = text;
            btn.addEventListener('click', function () {
                choose(i);
            });
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

        const buttons = Array.prototype.slice.call(elOpts.children);
        buttons.forEach(function (b, i) {
            b.disabled = true;
            if (i === picked) b.classList.add('picked');
        });

        const dot = elDots.children[state.index];
        if (dot) dot.classList.add('done');

        renderHud();
        setTimeout(nextQuestion, 220);
    }

    /* ---------------- 交卷 ---------------- */

    function submit() {
        if (state.submitting) return;
        state.submitting = true;
        stopTick();
        elText.textContent = '正在判分…';
        elOpts.innerHTML = '';

        request('/api/quiz/submit', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({roundId: state.roundId, answers: state.answers})
        }).then(function (res) {
            showResult(res);
        }).catch(function (e) {
            elText.textContent = '交卷失败：' + e.message;
        }).finally(function () {
            state.submitting = false;
        });
    }

    function showResult(res) {
        document.getElementById('r-score').textContent = String(res.score);
        document.getElementById('r-right').textContent = res.correct + ' / ' + res.total;
        document.getElementById('r-breakdown').textContent =
            '基础 ' + res.baseScore + ' + 速度 ' + res.speedBonus;
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
            head.textContent = (d.correct ? '✓ ' : '✗ ') + (i + 1) + '. ' + d.question;
            const body = document.createElement('div');
            body.className = 'detail-body';
            const yours = d.yourAnswer >= 0 ? letter(d.yourAnswer) : '未作答';
            body.textContent = '你的选择：' + yours + '　正确答案：' + letter(d.correctAnswer)
                + (d.explanation ? '　—　' + d.explanation : '');
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
        if (elPlay.hidden || state.answered) return;
        const key = e.key.toUpperCase();
        let pick = -1;
        if (key >= '1' && key <= '4') pick = Number(key) - 1;
        else if (key >= 'A' && key <= 'D') pick = key.charCodeAt(0) - 65;
        if (pick >= 0 && pick < elOpts.children.length) {
            e.preventDefault();
            choose(pick);
        }
    });

    loadCategories();
})();
