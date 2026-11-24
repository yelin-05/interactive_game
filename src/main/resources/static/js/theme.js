/* =====================================================================
   全站主题：默认「天蓝」浅色，可切「墨绿」深色；选择写入 localStorage
   必须在 <head> 里同步加载，避免首屏闪一下深色
   ===================================================================== */
(function () {
    var KEY = 'moshi-theme';
    var root = document.documentElement;

    function read() {
        try {
            return localStorage.getItem(KEY);
        } catch (e) {
            return null;
        }
    }

    function isDark() {
        return root.getAttribute('data-theme') === 'dark';
    }

    function apply(theme) {
        root.setAttribute('data-theme', theme === 'dark' ? 'dark' : 'light');
    }

    apply(read() || 'light');

    function paint(btn) {
        var dark = isDark();
        btn.textContent = dark ? '☀' : '☾';
        btn.setAttribute('aria-label', dark ? '切换到浅色' : '切换到深色');
        btn.setAttribute('title', dark ? '切换到浅色' : '切换到深色');
        btn.setAttribute('aria-pressed', dark ? 'true' : 'false');
    }

    function bind() {
        var btns = document.querySelectorAll('[data-theme-toggle]');
        if (!btns.length) {
            return;
        }
        Array.prototype.forEach.call(btns, function (btn) {
            paint(btn);
            btn.addEventListener('click', function () {
                apply(isDark() ? 'light' : 'dark');
                try {
                    localStorage.setItem(KEY, root.getAttribute('data-theme'));
                } catch (e) {
                    /* 隐私模式下忽略 */
                }
                Array.prototype.forEach.call(btns, paint);
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', bind);
    } else {
        bind();
    }
})();
