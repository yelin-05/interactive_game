/**
 * 模板 B：记忆翻牌
 * 16 张牌 8 对图案。得分 = 1000 − 步数×20 − 用时(秒)×5，最低 0 分。
 * 图案现在用 emoji 占位，接素材库后换成真实图片路径。
 */
(function () {
    'use strict';

    const ICONS = ['🗡️', '🛡️', '🧪', '🔮', '🏹', '🧭', '💰', '🗝️'];
    const PAIRS = ICONS.length;
    const BASE = 1000;

    const board = document.getElementById('board');
    const elTime = document.getElementById('hud-time');
    const elMoves = document.getElementById('hud-moves');
    const elPairs = document.getElementById('hud-pairs');
    const elScore = document.getElementById('hud-score');
    const btnStart = document.getElementById('btn-start');
    const btnAgain = document.getElementById('btn-again');
    const modal = document.getElementById('modal');

    let running = false;
    let firstCard = null;
    let lock = false;
    let moves = 0;
    let pairs = 0;
    let startAt = 0;
    let timerId = null;

    function shuffle(arr) {
        for (let i = arr.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [arr[i], arr[j]] = [arr[j], arr[i]];
        }
        return arr;
    }

    function elapsed() {
        return running || startAt ? Math.floor((Date.now() - startAt) / 1000) : 0;
    }

    function scoreNow() {
        const s = BASE - moves * 20 - elapsed() * 5;
        return Math.max(0, s);
    }

    function render() {
        elMoves.textContent = String(moves);
        elPairs.textContent = pairs + '/' + PAIRS;
        elScore.textContent = String(scoreNow());
    }

    function renderTime() {
        const s = elapsed();
        elTime.textContent = s + 's';
        elScore.textContent = String(scoreNow());
    }

    function buildBoard() {
        board.innerHTML = '';
        const deck = shuffle(ICONS.concat(ICONS));
        deck.forEach((icon) => {
            const card = document.createElement('div');
            card.className = 'mcard';
            card.dataset.key = icon;
            const face = document.createElement('span');
            face.className = 'face';
            face.textContent = icon;
            card.appendChild(face);
            card.addEventListener('click', () => onClick(card));
            board.appendChild(card);
        });
    }

    function onClick(card) {
        if (!running || lock) return;
        if (card.classList.contains('flip') || card.classList.contains('done')) return;
        card.classList.add('flip');

        if (!firstCard) {
            firstCard = card;
            return;
        }

        moves++;
        const a = firstCard;
        const b = card;
        firstCard = null;

        if (a.dataset.key === b.dataset.key) {
            a.classList.remove('flip');
            b.classList.remove('flip');
            a.classList.add('done');
            b.classList.add('done');
            pairs++;
            render();
            if (pairs === PAIRS) end(true);
        } else {
            lock = true;
            render();
            setTimeout(() => {
                a.classList.remove('flip');
                b.classList.remove('flip');
                a.classList.add('shake');
                b.classList.add('shake');
                setTimeout(() => {
                    a.classList.remove('shake');
                    b.classList.remove('shake');
                    lock = false;
                }, 200);
            }, 620);
        }
    }

    function start() {
        clearInterval(timerId);
        running = true;
        firstCard = null;
        lock = false;
        moves = 0;
        pairs = 0;
        startAt = Date.now();
        modal.hidden = true;
        btnStart.disabled = true;
        btnStart.textContent = '进行中…';
        buildBoard();
        render();
        elTime.textContent = '0s';
        timerId = setInterval(renderTime, 250);
    }

    function rankOf(score) {
        if (score >= 800) return '神级记忆';
        if (score >= 600) return '优秀';
        if (score >= 350) return '还行';
        return '再练练';
    }

    function end(won) {
        running = false;
        clearInterval(timerId);
        btnStart.disabled = false;
        btnStart.textContent = '开始挑战';
        const final = won ? scoreNow() : 0;
        render();
        document.getElementById('r-score').textContent = String(final);
        document.getElementById('r-moves').textContent = String(moves);
        document.getElementById('r-time').textContent = elapsed() + 's';
        document.getElementById('r-rank').textContent = won ? rankOf(final) : '未完成';
        modal.hidden = false;
    }

    buildBoard();
    render();
    btnStart.addEventListener('click', start);
    btnAgain.addEventListener('click', start);
})();
