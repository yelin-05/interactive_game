/**
 * /llm「设置」页前端：
 * - 「厂商」下拉 → 带出接口地址、JSON 开关、该厂商的模型档位卡片；
 * - 档位卡片 → 点一下写进真正提交的 name="model" 输入框（也能手改）；
 * - 标签页 → 接口配置 / 状态与试跑 两块切换，刷新后停在当前标签；
 * - 没有 JS 时：模型名照样能手填、表单照样能提交，两块内容同时展示。
 *
 * 只改表单已有字段，不替用户填 Key —— Key 始终由人手输，避免误用别人的额度。
 */
(function () {
    'use strict';

    var vendorSelect = document.getElementById('llm-vendor');
    var urlInput = document.getElementById('llm-base-url');
    var modelInput = document.getElementById('llm-model');
    var cardBox = document.getElementById('llm-model-cards');

    /**
     * 各厂商目录：
     *   url        —— OpenAI 兼容的 chat/completions 地址
     *   jsonFormat —— 是否支持 response_format=json_object
     *   models     —— 档位卡片：value 提交给后端，desc 一句话说明，
     *                 ctx 上下文长度，tier 定位（经济 / 均衡 / 旗舰 / 推理）
     */
    var VENDORS = {
        deepseek: {
            url: 'https://api.deepseek.com/v1/chat/completions',
            jsonFormat: true,
            models: [
                { value: 'deepseek-chat', desc: 'V3 通用对话，日常出题够用', ctx: '64K', tier: '经济' },
                { value: 'deepseek-reasoner', desc: 'R1 深度推理，出难题更稳', ctx: '64K', tier: '推理' }
            ]
        },
        qwen: {
            url: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
            jsonFormat: true,
            models: [
                { value: 'qwen-flash', desc: '最快最便宜，现场演示首选', ctx: '1M', tier: '经济' },
                { value: 'qwen-turbo', desc: '低价通用，响应快', ctx: '1M', tier: '经济' },
                { value: 'qwen-plus', desc: '能力与价格均衡', ctx: '128K', tier: '均衡' },
                { value: 'qwen-max', desc: '旗舰档，复杂题更准', ctx: '32K', tier: '旗舰' }
            ]
        },
        zhipu: {
            url: 'https://open.bigmodel.cn/api/paas/v4/chat/completions',
            jsonFormat: true,
            models: [
                { value: 'glm-4-flash', desc: '免费额度，速度最快', ctx: '128K', tier: '经济' },
                { value: 'glm-4-air', desc: '轻量均衡，日常够用', ctx: '128K', tier: '均衡' },
                { value: 'glm-4-plus', desc: '更强，价格也更高', ctx: '128K', tier: '旗舰' }
            ]
        },
        moonshot: {
            url: 'https://api.moonshot.cn/v1/chat/completions',
            jsonFormat: true,
            models: [
                { value: 'kimi-latest', desc: '自动指向最新版本', ctx: '动态', tier: '均衡' },
                { value: 'moonshot-v1-8k', desc: '短上下文，便宜', ctx: '8K', tier: '经济' },
                { value: 'moonshot-v1-32k', desc: '中长上下文', ctx: '32K', tier: '均衡' },
                { value: 'moonshot-v1-128k', desc: '超长上下文，价格高', ctx: '128K', tier: '旗舰' }
            ]
        },
        openai: {
            url: 'https://api.openai.com/v1/chat/completions',
            jsonFormat: true,
            models: [
                { value: 'gpt-4o-mini', desc: '便宜快，演示够用', ctx: '128K', tier: '经济' },
                { value: 'gpt-4o', desc: '能力更强，价格更高', ctx: '128K', tier: '旗舰' }
            ]
        },
        ollama: {
            url: 'http://localhost:11434/v1/chat/completions',
            jsonFormat: false,
            models: [
                { value: 'qwen2.5:7b', desc: '中文不错，8G 显存能跑', ctx: '32K', tier: '经济' },
                { value: 'qwen2.5:14b', desc: '更强，需要更多显存', ctx: '32K', tier: '均衡' },
                { value: 'llama3.1:8b', desc: '通用英文强', ctx: '128K', tier: '均衡' },
                { value: 'deepseek-r1:7b', desc: '本地推理模型', ctx: '64K', tier: '推理' }
            ]
        },
        custom: {
            url: '',
            jsonFormat: true,
            models: []
        }
    };

    /** 从地址反推厂商，用于打开页面时回显厂商下拉 */
    function vendorOf(url) {
        var u = (url || '').toLowerCase();
        if (!u) {
            return 'custom';
        }
        if (u.indexOf('deepseek') >= 0) {
            return 'deepseek';
        }
        if (u.indexOf('dashscope') >= 0 || u.indexOf('aliyuncs') >= 0) {
            return 'qwen';
        }
        if (u.indexOf('bigmodel.cn') >= 0 || u.indexOf('zhipu') >= 0) {
            return 'zhipu';
        }
        if (u.indexOf('moonshot') >= 0 || u.indexOf('kimi') >= 0) {
            return 'moonshot';
        }
        if (u.indexOf('openai.com') >= 0) {
            return 'openai';
        }
        if (u.indexOf('11434') >= 0 || u.indexOf('ollama') >= 0) {
            return 'ollama';
        }
        return 'custom';
    }

    function jsonFormatBox() {
        return document.querySelector('input[name="jsonFormat"]');
    }

    function modelsOf(vendor) {
        var cfg = VENDORS[vendor];
        return (cfg && cfg.models) ? cfg.models : [];
    }

    function findModel(vendor, value) {
        var list = modelsOf(vendor);
        for (var i = 0; i < list.length; i++) {
            if (list[i].value === value) {
                return list[i];
            }
        }
        return null;
    }

    function chip(text, tone) {
        var el = document.createElement('span');
        el.className = 'chip' + (tone ? ' ' + tone : '');
        el.textContent = text;
        return el;
    }

    /** 模型名与卡片一致就高亮；手填目录外的名字则全部不高亮 */
    function paintSelection() {
        if (!cardBox || !modelInput) {
            return;
        }
        var current = modelInput.value.trim();
        var cards = cardBox.querySelectorAll('.model-card');
        Array.prototype.forEach.call(cards, function (card) {
            var on = card.getAttribute('data-model') === current;
            card.classList.toggle('on', on);
            card.setAttribute('aria-pressed', on ? 'true' : 'false');
        });
    }

    /** 按厂商重建档位卡片 */
    function renderCards(vendor) {
        if (!cardBox) {
            return;
        }
        cardBox.innerHTML = '';
        var list = modelsOf(vendor);
        if (!list.length) {
            var tip = document.createElement('p');
            tip.className = 'help';
            tip.style.margin = '0';
            tip.textContent = '这个厂商没有内置目录，直接把模型名填到上面的「模型」里就行。';
            cardBox.appendChild(tip);
            paintSelection();
            return;
        }
        list.forEach(function (m) {
            var card = document.createElement('button');
            card.type = 'button';
            card.className = 'model-card';
            card.setAttribute('data-model', m.value);
            card.setAttribute('aria-pressed', 'false');

            var name = document.createElement('span');
            name.className = 'model-name';
            name.textContent = m.value;

            var desc = document.createElement('span');
            desc.className = 'model-desc';
            desc.textContent = m.desc || '';

            var meta = document.createElement('span');
            meta.className = 'model-meta';
            meta.appendChild(chip('上下文 ' + (m.ctx || '未知'), ''));
            meta.appendChild(chip(m.tier || '模型', m.tier === '经济' ? 'ok' : 'accent'));

            card.appendChild(name);
            card.appendChild(desc);
            card.appendChild(meta);
            card.addEventListener('click', function () {
                modelInput.value = m.value;
                paintSelection();
                modelInput.focus();
            });
            cardBox.appendChild(card);
        });
        paintSelection();
    }

    /* ---------------- 初始化：地址反推厂商 → 渲染档位 ---------------- */
    if (vendorSelect && urlInput && modelInput) {
        var initialVendor = vendorOf(urlInput.value);
        if (VENDORS[initialVendor]) {
            vendorSelect.value = initialVendor;
        }
        renderCards(vendorSelect.value);

        // 换厂商：带出地址 / JSON 开关 / 该厂商档位
        vendorSelect.addEventListener('change', function () {
            var vendor = vendorSelect.value;
            var cfg = VENDORS[vendor];
            if (!cfg || vendor === 'custom') {
                renderCards('custom');
                urlInput.focus();
                return;
            }
            urlInput.value = cfg.url;
            var json = jsonFormatBox();
            if (json) {
                json.checked = cfg.jsonFormat !== false;
            }
            // 旧模型名属于别家 → 换成这家的第一个档位
            if (!findModel(vendor, modelInput.value.trim())) {
                var first = modelsOf(vendor)[0];
                if (first) {
                    modelInput.value = first.value;
                }
            }
            renderCards(vendor);
        });

        // 手改模型名：命中目录就高亮对应卡片
        modelInput.addEventListener('input', paintSelection);
        modelInput.addEventListener('change', paintSelection);

        // 手改地址：反推厂商，档位跟着换（不动已填的模型名）
        urlInput.addEventListener('change', function () {
            var vendor = vendorOf(urlInput.value);
            if (VENDORS[vendor]) {
                vendorSelect.value = vendor;
            }
            renderCards(vendorSelect.value);
        });
    }

    /* ---------------- 标签页：接口配置 / 状态与试跑 ---------------- */
    (function () {
        var tabBox = document.getElementById('llm-tabs');
        if (!tabBox) {
            return;
        }
        var tabs = tabBox.querySelectorAll('[data-tab]');
        var panels = document.querySelectorAll('[data-panel]');
        if (!tabs.length || !panels.length) {
            return;
        }

        // 两块内容都在同一页，切标签只切显隐；不依赖 JS 时默认全展示
        function show(name) {
            Array.prototype.forEach.call(panels, function (panel) {
                if (panel.getAttribute('data-panel') === name) {
                    panel.removeAttribute('hidden');
                } else {
                    panel.setAttribute('hidden', '');
                }
            });
            Array.prototype.forEach.call(tabs, function (tab) {
                tab.classList.toggle('on', tab.getAttribute('data-tab') === name);
            });
        }

        Array.prototype.forEach.call(tabs, function (tab) {
            tab.addEventListener('click', function () {
                var name = tab.getAttribute('data-tab');
                show(name);
                try {
                    // 刷新后停在同一个标签
                    history.replaceState(null, '', '#' + name);
                } catch (e) {
                    /* 某些环境不支持，忽略即可 */
                }
            });
        });

        show((location.hash || '').replace('#', '') === 'status' ? 'status' : 'config');
    })();
})();

