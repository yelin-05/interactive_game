/**
 * 首页「系统自检」：调 /api/ping，把后端链路状态摊开给人看。
 * 答辩时没网也能用 —— 页面和样式全部本地，不依赖任何 CDN。
 */
(function () {
    'use strict';

    const btn = document.getElementById('btn-check');
    const panel = document.getElementById('panel');
    const tbody = document.getElementById('kv-body');
    const verdict = document.getElementById('verdict');
    const hintEl = document.getElementById('hint');

    const ROWS = [
        {label: '应用名称', key: 'app'},
        {label: '版本', key: 'version'},
        {label: 'JDK 版本', key: 'javaVersion'},
        {label: '服务器时间', key: 'serverTime'},
        {label: '数据库状态', key: 'dbStatus', stateful: true},
        {label: '数据库类型', key: 'dbProduct'},
        {label: '数据库版本', key: 'dbVersion'},
        {label: '用户数', key: 'userCount'}
    ];

    function render(data) {
        tbody.innerHTML = '';
        ROWS.forEach(function (row) {
            const tr = document.createElement('tr');
            const th = document.createElement('td');
            const td = document.createElement('td');
            th.textContent = row.label;

            let value = data[row.key];
            // LocalDateTime 序列化出来是 2026-10-21T16:56:23.0540899，去掉 T 和纳秒
            if (row.key === 'serverTime' && typeof value === 'string') {
                value = value.replace('T', ' ').split('.')[0];
            }
            value = (value === null || value === undefined || value === '') ? '-' : String(value);
            td.textContent = value;

            if (row.stateful) {
                const up = value === 'UP';
                td.className = up ? 'val-ok' : 'val-bad';
            }
            tr.appendChild(th);
            tr.appendChild(td);
            tbody.appendChild(tr);
        });

        const dbUp = data.dbStatus === 'UP';
        verdict.textContent = dbUp ? '链路正常 · PASS' : '数据库未连通 · FAIL';
        verdict.className = 'pill ' + (dbUp ? 'ok' : 'bad');

        if (dbUp) {
            hintEl.textContent = 'Web 容器 → Controller → Service → MyBatis-Plus → 数据源 全链路已打通，可以开始做业务模块了。';
        } else {
            hintEl.textContent = '接口本身是通的，只是数据库没连上：' + (data.dbError || '未知原因');
        }
    }

    function fail(err) {
        verdict.textContent = '请求失败 · FAIL';
        verdict.className = 'pill bad';
        hintEl.textContent = '调用 /api/ping 失败：' + err;
        tbody.innerHTML = '';
    }

    async function check() {
        btn.disabled = true;
        btn.textContent = '检测中…';
        panel.hidden = false;
        try {
            const resp = await fetch('/api/ping', {headers: {'Accept': 'application/json'}});
            const json = await resp.json();
            if (!resp.ok || json.code !== 0) {
                throw new Error('code=' + json.code + ' ' + (json.message || ''));
            }
            render(json.data);
        } catch (e) {
            fail(e.message || e);
        } finally {
            btn.disabled = false;
            btn.textContent = '系统自检';
        }
    }

    if (btn) {
        btn.addEventListener('click', check);
    }

    // 支持深链直接触发自检：http://localhost:8080/?check=1
    if (new URLSearchParams(window.location.search).has('check')) {
        check();
    }
})();
