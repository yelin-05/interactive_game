/**
 * 模板 A：打地鼠
 * 30 秒内点中随机冒头的地鼠。单次得分 = 10 + (连击-1)×2，点空坑扣 5 分并清空连击。
 */
(function () {
    'use strict';

    const HOLES = 9;
    const DURATION = 30;
    const MOLE_LIFE = 900;
    const SPAWN_MIN = 380;
    const SPAWN_MAX = 680;

    const board = document.getElementById('board');
    const elTime = document.getElementById('hud-time');
    const elScore = document.getElementById('hud-score');
    const elCombo = document.getElementById('hud-combo');
    const elAcc = document.getElementById('hud-acc');
    const btnStart = document.getElementById('btn-start');
    const btnAgain = document.getElementById('btn-again');
    const modal = document.getElementById('modal');

    const holes = [];
    let running = false;
    let score = 0;
    let combo = 0;
    let maxCombo = 0;
    let hits = 0;
    let clicks = 0;
    let timeLeft = DURATION;
    let activeIndex = -1;
    let tickTimer = null;
    let spawnTimer = null;
    let moleTimer = null;

    function rand(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }

    function buildBoard() {
        for (let i = 0; i < HOLES; i++) {
            const hole = document.createElement('div');
            hole.className = 'hole';
            hole.dataset.index = String(i);
            const mole = document.createElement('span');
            mole.className = 'mole';
            mole.textContent = '🐹';
            hole.appendChild(mole);
            hole.addEventListener('click', () => onClick(i));
            board.appendChild(hole);
            holes.push(hole);
        }
    }

    function accuracy() {
        return clicks === 0 ? '--' : Math.round((hits / clicks) * 100) + '%';
    }

    function render() {
        elTime.textContent = String(Math.max(0, timeLeft));
        elTime.className = timeLeft <= 5 ? 'bad' : (timeLeft <= 10 ? 'warn' : '');
        elScore.textContent = String(score);
        elCombo.textContent = String(combo);
        elAcc.textContent = accuracy();
    }

    function hideMole(i) {
        if (i < 0) return;
        holes[i].classList.remove('active');
        if (activeIndex === i) activeIndex = -1;
    }

    function popMole() {
        hideMole(activeIndex);
        clearTimeout(moleTimer);
        activeIndex = rand(0, HOLES - 1);
        holes[activeIndex].classList.add('active');
        moleTimer = setTimeout(() => {
            if (running) hideMole(activeIndex);
        }, MOLE_LIFE);
    }

    function scheduleSpawn() {
        spawnTimer = setTimeout(() => {
            if (!running) return;
            popMole();
            scheduleSpawn();
        }, rand(SPAWN_MIN, SPAWN_MAX));
    }

    function onClick(i) {
        if (!running) return;
        clicks++;

        if (i === activeIndex) {
            hits++;
            combo++;
            maxCombo = Math.max(maxCombo, combo);
            score += 10 + (combo - 1) * 2;
            clearTimeout(moleTimer);
            hideMole(i);
            const hole = holes[i];
            hole.classList.add('pop');
            setTimeout(() => hole.classList.remove('pop'), 180);
        } else {
            combo = 0;
            score = Math.max(0, score - 5);
            const hole = holes[i];
            hole.classList.add('miss');
            setTimeout(() => hole.classList.remove('miss'), 180);
        }
        render();
    }

    function stopTimers() {
        clearInterval(tickTimer);
        clearTimeout(spawnTimer);
        clearTimeout(moleTimer);
        tickTimer = spawnTimer = moleTimer = null;
    }

    function start() {
        stopTimers();
        holes.forEach((h, i) => hideMole(i));
        running = true;
        score = 0;
        combo = 0;
        maxCombo = 0;
        hits = 0;
        clicks = 0;
        timeLeft = DURATION;
        activeIndex = -1;
        modal.hidden = true;
        btnStart.disabled = true;
        btnStart.textContent = '进行中…';
        render();

        tickTimer = setInterval(() => {
            timeLeft--;
            render();
            if (timeLeft <= 0) end();
        }, 1000);

        popMole();
        scheduleSpawn();
    }

    function end() {
        running = false;
        stopTimers();
        holes.forEach((h, i) => hideMole(i));
        btnStart.disabled = false;
        btnStart.textContent = '开始 · 30 秒';

        document.getElementById('r-score').textContent = String(score);
        document.getElementById('r-hit').textContent = hits + ' / ' + clicks;
        document.getElementById('r-acc').textContent = accuracy();
        document.getElementById('r-combo').textContent = String(maxCombo);
        modal.hidden = false;
    }

    buildBoard();
    render();
    btnStart.addEventListener('click', start);
    btnAgain.addEventListener('click', start);

    // 演示/截图用：/games/reaction?auto=1 直接开局
    if (new URLSearchParams(location.search).has('auto')) {
        start();
    }
})();
