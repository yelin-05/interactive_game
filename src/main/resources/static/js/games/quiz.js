/**
 * 模板 C：知识闯关
 * 5 道单选，每题 15 秒。答对得 10 分 + 剩余秒数（最多 25 分/题）。
 * 题目现在是写死的，接大模型后可按素材内容动态生成。
 */
(function () {
    'use strict';

    const LIMIT = 15;

    const QUESTIONS = [
        {
            topic: 'Spring Boot',
            q: 'Spring Boot 应用内嵌的 Servlet 容器，默认是下面哪一个？',
            options: ['Tomcat', 'Jetty', 'Undertow', 'Netty'],
            answer: 0,
            explain: 'spring-boot-starter-web 默认内嵌 Tomcat；换成 Jetty 或 Undertow 只需排除默认依赖再引入对应的 starter。'
        },
        {
            topic: 'MyBatis-Plus',
            q: 'MyBatis-Plus 中提供单表 CRUD 通用方法的接口是？',
            options: ['JpaRepository', 'BaseMapper', 'CrudRepository', 'MapperTemplate'],
            answer: 1,
            explain: '继承 BaseMapper 就自带 selectById、selectList、insert、updateById、deleteById 等方法，不用写 XML。'
        },
        {
            topic: 'HTTP',
            q: 'HTTP 状态码 404 表示什么？',
            options: ['服务器内部错误', '请求的资源不存在', '需要重定向', '请求方法不被允许'],
            answer: 1,
            explain: '404 Not Found 是资源不存在；500 才是服务端内部错误，302 是重定向，405 是方法不允许。'
        },
        {
            topic: 'MySQL',
            q: 'MySQL 里 utf8mb4 相比 utf8 的主要优势是？',
            options: ['查询速度更快', '支持 emoji 等 4 字节字符', '占用存储更小', '支持事务'],
            answer: 1,
            explain: 'MySQL 的 utf8 实际是 utf8mb3，最长 3 字节，存不了 emoji；utf8mb4 才是完整的 UTF-8。'
        },
        {
            topic: 'Spring',
            q: '把一个普通类交给 Spring 容器管理，最常用的注解是？',
            options: ['@Entity', '@Table', '@Component', '@Mapper'],
            answer: 2,
            explain: '@Component 及其派生注解（@Service / @Controller / @Repository）用于声明 Bean；@Entity 和 @Table 是 JPA 的持久化注解。'
        }
    ];

    const elQ = document.getElementById('hud-q');
    const elScore = document.getElementById('hud-score');
    const elTime = document.getElementById('hud-time');
    const elRight = document.getElementById('hud-right');
    const elIdx = document.getElementById('q-idx');
    const elTopic = document.getElementById('q-topic');
    const elText = document.getElementById('q-text');
    const elOpts = document.getElementById('opts');
    const elBar = document.getElementById('q-bar');
    const elExplain = document.getElementById('explain');
    const btnStart = document.getElementById('btn-start');
    const btnAgain = document.getElementById('btn-again');
    const modal = document.getElementById('modal');

    let index = -1;
    let score = 0;
    let rightCount = 0;
    let remain = LIMIT;
    let tickId = null;
    let answered = false;
    let usedTime = 0;

    function renderHud() {
        elQ.textContent = (index < 0 ? '-' : index + 1) + '/' + QUESTIONS.length;
        elScore.textContent = String(score);
        elRight.textContent = String(rightCount);
        elIdx.textContent = '第 ' + Math.max(0, index + 1) + ' / ' + QUESTIONS.length + ' 题';
        elTime.textContent = remain >= 0 ? remain + 's' : '--';
        elTime.className = remain <= 5 ? 'bad' : (remain <= 8 ? 'warn' : '');
        elBar.style.width = Math.max(0, (remain / LIMIT) * 100) + '%';
    }

    function stopTick() {
        clearInterval(tickId);
        tickId = null;
    }

    function showQuestion() {
        answered = false;
        remain = LIMIT;
        const q = QUESTIONS[index];
        elTopic.textContent = q.topic;
        elText.textContent = q.q;
        elExplain.hidden = true;
        elExplain.textContent = '';
        elOpts.innerHTML = '';

        q.options.forEach((text, i) => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'opt';
            btn.textContent = String.fromCharCode(65 + i) + '. ' + text;
            btn.addEventListener('click', () => answer(i));
            elOpts.appendChild(btn);
        });

        renderHud();
        stopTick();
        tickId = setInterval(() => {
            remain--;
            if (remain <= 0) {
                remain = 0;
                renderHud();
                answer(-1);
                return;
            }
            renderHud();
        }, 1000);
    }

    function answer(picked) {
        if (answered) return;
        answered = true;
        stopTick();

        const q = QUESTIONS[index];
        const buttons = Array.from(elOpts.children);
        buttons.forEach((b, i) => {
            b.disabled = true;
            if (i === q.answer) b.classList.add('correct');
            if (i === picked && picked !== q.answer) b.classList.add('wrong');
        });

        if (picked === q.answer) {
            rightCount++;
            score += 10 + remain;
            usedTime += LIMIT - remain;
            elExplain.textContent = '✅ 答对了，+' + (10 + remain) + ' 分（基础 10 + 时间加成 ' + remain + '）。' + q.explain;
        } else {
            usedTime += LIMIT;
            const tip = picked === -1 ? '⏱ 时间到。' : '❌ 答错了。';
            elExplain.textContent = tip + '正确答案是 ' + String.fromCharCode(65 + q.answer) + '。' + q.explain;
        }
        elExplain.hidden = false;
        renderHud();

        setTimeout(() => {
            if (index + 1 >= QUESTIONS.length) {
                end();
            } else {
                index++;
                showQuestion();
            }
        }, 1500);
    }

    function start() {
        stopTick();
        index = 0;
        score = 0;
        rightCount = 0;
        usedTime = 0;
        modal.hidden = true;
        btnStart.disabled = true;
        btnStart.textContent = '进行中…';
        showQuestion();
    }

    function rankOf(right) {
        if (right === QUESTIONS.length) return 'S · 满分';
        if (right === 4) return 'A · 优秀';
        if (right === 3) return 'B · 及格';
        return 'C · 再练练';
    }

    function end() {
        stopTick();
        btnStart.disabled = false;
        btnStart.textContent = '重新开始';
        elTime.textContent = '--';
        elBar.style.width = '0%';
        document.getElementById('r-score').textContent = String(score);
        document.getElementById('r-right').textContent = rightCount + ' / ' + QUESTIONS.length;
        document.getElementById('r-avg').textContent = Math.round(usedTime / QUESTIONS.length) + 's';
        document.getElementById('r-rank').textContent = rankOf(rightCount);
        modal.hidden = false;
    }

    renderHud();
    btnStart.addEventListener('click', start);
    btnAgain.addEventListener('click', start);

    // 演示/截图用：/games/quiz?auto=1 直接开局
    if (new URLSearchParams(location.search).has('auto')) {
        start();
    }
})();
