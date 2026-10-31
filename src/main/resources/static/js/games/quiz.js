/**
 * 模板 C：知识闯关
 *
 * 流程：加载分区 → 选分区拉题 → 逐题作答 → 结算。
 * 题目全部来自后端 /api/quiz/questions，前端不再内置题库。
 *
 * 计分：答对 = 10 分基础分 + 本题剩余秒数（最高 25 分/题），连对额外 +2/连。
 */
(function () {
    'use strict';

    const LIMIT = 15;          // 每题秒数
    const PER_ROUND = 5;       // 每局题数
    const NEXT_DELAY = 1400;   // 答完停留多久进入下一题

    const elPick = document.getElementById('view-pick');
    const elPlay = document.getElementById('view-play');
    const elGrid = document.getElementById('pick-grid');
    const elPickTip = document.getElementById('pick-tip');

    const elScore = document.getElementById('hud-score');
    const elTime = document.getElementById('hud-time');
    const elRight = document.getElementById('hud-right');
    const elStreak = document.getElementById('hud-streak');
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
        questions: [],
        index: -1,
        score: 0,
        right: 0,
        streak: 0,
        maxStreak: 0,
        usedTime: 0,
        remain: LIMIT,
        answered: false,
        tickId: null
    };

    /* ---------------- 通用 ---------------- */

    function api(path) {
        return fetch(path, {headers: {'Accept': 'application/json'}})
            .then(function (resp) {
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
        return api('/api/quiz/categories').then(function (list) {
            state.categories = list;
            renderCategories();
            elPickTip.textContent = '共 ' + list.reduce(function (n, c) {
                return n + c.count;
            }, 0) + ' 道题，分 ' + list.length + ' 个区。题目存在数据库里，可以随时扩充。';

            // 支持 /games/quiz?category=history 直接开局
            const wanted = new URLSearchParams(location.search).get('category');
            if (wanted) {
                const hit = list.find(function (c) {
                    return c.code === wanted;
                });
                if (hit) startGame(hit);
            }
        }).catch(function (e) {
            elGrid.innerHTML = '';
            elPickTip.textContent = '题库加载失败：' + e.message + '（确认后端已启动、数据库连得上）';
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

    /* ---------------- 答题 ---------------- */

    function startGame(category) {
        modal.hidden = true;
        elPick.hidden = true;
        elPlay.hidden = false;
        elText.textContent = '正在抽题…';
        elOpts.innerHTML = '';
        elExplain.hidden = true;
        elDots.innerHTML = '';

        api('/api/quiz/questions?category=' + encodeURIComponent(category.code) + '&limit=' + PER_ROUND)
            .then(function (list) {
                if (!list.length) {
                    elText.textContent = '这个分区还没有题目。';
                    return;
                }
                state.current = category;
                state.questions = list;
                state.index = -1;
                state.score = 0;
                state.right = 0;
                state.streak = 0;
                state.maxStreak = 0;
                state.usedTime = 0;

                elCat.textContent = category.icon + ' ' + category.name;
                elDots.innerHTML = '';
                list.forEach(function () {
                    const d = document.createElement('i');
                    d.className = 'dot';
                    elDots.appendChild(d);
                });

                renderHud();
                nextQuestion();
            })
            .catch(function (e) {
                elText.textContent = '抽题失败：' + e.message;
            });
    }

    function renderHud() {
        elScore.textContent = String(state.score);
        elRight.textContent = String(state.right);
        elStreak.textContent = String(state.streak);
        elTime.textContent = state.answered ? '--' : String(state.remain);
        elTime.className = state.answered ? '' : (state.remain <= 5 ? 'bad' : (state.remain <= 8 ? 'warn' : ''));
        elIdx.textContent = '第 ' + Math.max(0, state.index + 1) + ' / ' + state.questions.length + ' 题';
        elBar.style.width = (state.answered ? 0 : (state.remain / LIMIT) * 100) + '%';

        const dots = elDots.children;
        for (let i = 0; i < dots.length; i++) {
            dots[i].classList.toggle('now', i === state.index && !state.answered);
        }
    }

    function nextQuestion() {
        state.index++;
        if (state.index >= state.questions.length) {
            end();
            return;
        }
        const q = state.questions[state.index];
        state.remain = LIMIT;
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
                answer(i);
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
                answer(-1);
                return;
            }
            renderHud();
        }, 1000);
    }

    function answer(picked) {
        if (state.answered) return;
        state.answered = true;
        stopTick();

        const q = state.questions[state.index];
        const buttons = Array.prototype.slice.call(elOpts.children);
        buttons.forEach(function (b, i) {
            b.disabled = true;
            if (i === q.answerIndex) b.classList.add('correct');
            if (i === picked && picked !== q.answerIndex) b.classList.add('wrong');
        });

        const dot = elDots.children[state.index];
        const correct = picked === q.answerIndex;

        if (correct) {
            state.right++;
            state.streak++;
            state.maxStreak = Math.max(state.maxStreak, state.streak);
            state.usedTime += LIMIT - state.remain;
            const gain = 10 + state.remain + (state.streak > 1 ? 2 : 0);
            state.score += gain;
            if (dot) dot.classList.add('ok');
            elExplain.textContent = '✅ 答对了 +' + gain + ' 分'
                + (state.streak > 1 ? '（连对 ' + state.streak + '）' : '') + ' ' + (q.explanation || '');
        } else {
            state.streak = 0;
            state.usedTime += LIMIT;
            if (dot) dot.classList.add('bad');
            const tip = picked === -1 ? '⏱ 时间到。' : '❌ 答错了。';
            elExplain.textContent = tip + ' 正确答案是 ' + letter(q.answerIndex) + '。' + (q.explanation || '');
        }
        elExplain.hidden = false;
        renderHud();

        setTimeout(function () {
            if (state.index + 1 >= state.questions.length) {
                end();
            } else {
                nextQuestion();
            }
        }, NEXT_DELAY);
    }

    /* ---------------- 结算 ---------------- */

    function rankOf(right, total) {
        if (right === total) return 'S · 满分';
        if (right >= total - 1) return 'A · 优秀';
        if (right >= Math.ceil(total * 0.6)) return 'B · 及格';
        return 'C · 再练练';
    }

    function end() {
        stopTick();
        const total = state.questions.length;
        document.getElementById('r-cat').textContent = state.current.icon + ' ' + state.current.name;
        document.getElementById('r-score').textContent = String(state.score);
        document.getElementById('r-right').textContent = state.right + ' / ' + total;
        document.getElementById('r-streak').textContent = String(state.maxStreak);
        document.getElementById('r-avg').textContent = Math.round(state.usedTime / Math.max(1, total)) + 's';
        document.getElementById('r-rank').textContent = rankOf(state.right, total);
        document.getElementById('r-sub').textContent = total + ' 题总分';
        modal.hidden = false;
    }

    function backToPick() {
        stopTick();
        modal.hidden = true;
        elPlay.hidden = true;
        elPick.hidden = false;
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

    btnBack.addEventListener('click', backToPick);

    document.addEventListener('keydown', function (e) {
        if (elPlay.hidden || state.answered) return;
        const key = e.key.toUpperCase();
        let pick = -1;
        if (key >= '1' && key <= '4') pick = Number(key) - 1;
        else if (key >= 'A' && key <= 'D') pick = key.charCodeAt(0) - 65;
        if (pick >= 0 && pick < elOpts.children.length) {
            e.preventDefault();
            answer(pick);
        }
    });

    loadCategories();
})();
