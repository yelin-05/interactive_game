/**
 * 密码框增强：
 *   1) 每个 password 输入框右侧加一个「小眼睛」按钮，点一下在明文/密文之间切换；
 *   2) 注册页有两遍密码时，实时提示两次输入是否一致。
 *
 * 纯原生实现，没有密码框的页面自动跳过；不改表单 name，提交逻辑完全不受影响。
 */
(function () {
    'use strict';

    var EYE = '👁';
    var EYE_OFF = '🙈';

    function enhance(input) {
        if (input.dataset.pwReady === '1') {
            return;
        }
        input.dataset.pwReady = '1';

        // 包一层定位容器，按钮绝对定位在输入框右侧
        var wrap = document.createElement('span');
        wrap.className = 'pw-wrap';
        input.parentNode.insertBefore(wrap, input);
        wrap.appendChild(input);

        var btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'pw-toggle';
        btn.textContent = EYE;
        btn.title = '显示密码';
        btn.setAttribute('aria-label', '显示密码');
        btn.setAttribute('aria-pressed', 'false');

        btn.addEventListener('click', function () {
            var show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            btn.textContent = show ? EYE_OFF : EYE;
            var label = show ? '隐藏密码' : '显示密码';
            btn.title = label;
            btn.setAttribute('aria-label', label);
            btn.setAttribute('aria-pressed', show ? 'true' : 'false');
            input.focus({preventScroll: true});
            // 切到明文后把光标放末尾，方便继续改
            if (show && input.setSelectionRange) {
                try {
                    input.setSelectionRange(input.value.length, input.value.length);
                } catch (e) {
                    // 某些浏览器对密码框调用 setSelectionRange 会抛异常，忽略即可
                }
            }
        });

        wrap.appendChild(btn);
        input.classList.add('pw-input');
    }

    function addMatchHint() {
        var pwd = document.querySelector('input[name="password"]');
        var pwd2 = document.querySelector('input[name="password2"]');
        if (!pwd || !pwd2) {
            return;
        }
        var host = pwd2.closest('.field') || pwd2.parentNode;

        var hint = document.createElement('div');
        hint.className = 'pw-match';
        hint.hidden = true;
        host.appendChild(hint);

        function check() {
            if (!pwd2.value) {
                hint.hidden = true;
                pwd2.classList.remove('input-bad');
                return;
            }
            var same = pwd.value === pwd2.value;
            hint.hidden = false;
            hint.textContent = same ? '✓ 两次输入一致' : '✗ 两次输入不一致，请再核对一遍';
            hint.className = 'pw-match ' + (same ? 'good' : 'bad');
            pwd2.classList.toggle('input-bad', !same);
        }

        pwd.addEventListener('input', check);
        pwd2.addEventListener('input', check);
    }

    function init() {
        document.querySelectorAll('input[type="password"]').forEach(enhance);
        addMatchHint();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
