/* ============================================================
   Claude 风格聊天页面 · 交互逻辑
   对接后端现有接口（不改动后端）：
     GET  /chat/getChatList                    -> [{conversationId, title}]
     GET  /chat/getChatHistory?conversationId= -> [{role, content}]
     GET  /chat/stream?prompt=&conversationId= -> 流式纯文本
     GET  /chat/deleteChat?conversationId=     -> boolean
   会话元数据保存在浏览器 localStorage，后端负责消息记忆。
   ============================================================ */
(function () {
    'use strict';

    var API = {
        list: '/chat/getChatList',
        history: '/chat/getChatHistory',
        stream: '/chat/stream',
        del: '/chat/deleteChat'
    };
    var LS_KEY = 'claude_chat_sessions_v1';
    var STAR_SVG = '<svg viewBox="0 0 32 32" aria-hidden="true"><g fill="currentColor">' +
        '<rect x="14.8" y="1" width="2.4" height="30" rx="1.2"/>' +
        '<rect x="14.8" y="1" width="2.4" height="30" rx="1.2" transform="rotate(45 16 16)"/>' +
        '<rect x="14.8" y="1" width="2.4" height="30" rx="1.2" transform="rotate(90 16 16)"/>' +
        '<rect x="14.8" y="1" width="2.4" height="30" rx="1.2" transform="rotate(135 16 16)"/>' +
        '</g></svg>';
    var CHAT_ICON = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.4 8.4 0 0 1-9 8.4 9 9 0 0 1-3.9-.9L3 21l1.9-4.6A8.4 8.4 0 0 1 4 11.5a8.5 8.5 0 0 1 8.5-8.5h.5a8.5 8.5 0 0 1 8 8.5z"/></svg>';

    function $(id) { return document.getElementById(id); }
    var els = {
        sidebar: $('sidebar'), overlay: $('overlay'),
        brandLogo: $('brandLogo'), newChatBtn: $('newChatBtn'), sidebarClose: $('sidebarClose'),
        chatList: $('chatList'), menuBtn: $('menuBtn'),
        topbarTitle: $('topbarTitle'), messages: $('messages'),
        input: $('input'), sendBtn: $('sendBtn'), stopBtn: $('stopBtn'),
        errorBar: $('errorBar'), errorText: $('errorText'), errorClose: $('errorClose')
    };

    var state = {
        sessions: [],
        currentId: null,
        streaming: false,
        controller: null,
        assistantNode: null,
        assistantText: '',
        cursor: null
    };
    var errorTimer = null;

    /* ---------------- 工具 ---------------- */
    function genId() {
        try {
            if (window.crypto && crypto.randomUUID) return 'conv-' + crypto.randomUUID();
        } catch (e) { /* 某些环境禁用 crypto，走降级 */ }
        return 'conv-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 8);
    }

    function escapeHtml(s) {
        return String(s == null ? '' : s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function loadSessions() {
        try {
            var data = JSON.parse(localStorage.getItem(LS_KEY) || '[]');
            return Array.isArray(data) ? data : [];
        } catch (e) { return []; }
    }

    function saveSessions() {
        try { localStorage.setItem(LS_KEY, JSON.stringify(state.sessions)); } catch (e) { /* 隐私模式忽略 */ }
    }

    function currentSession() {
        for (var i = 0; i < state.sessions.length; i++) {
            if (state.sessions[i].id === state.currentId) return state.sessions[i];
        }
        return null;
    }

    function scrollBottom() {
        els.messages.scrollTop = els.messages.scrollHeight;
    }

    function showError(msg) {
        if (!msg) { els.errorBar.hidden = true; return; }
        els.errorText.textContent = msg;
        els.errorBar.hidden = false;
        clearTimeout(errorTimer);
        errorTimer = setTimeout(function () { els.errorBar.hidden = true; }, 9000);
    }

    /* ---------------- 视图渲染 ---------------- */
    function renderSessions() {
        var list = state.sessions.slice().sort(function (a, b) {
            return (b.updatedAt || 0) - (a.updatedAt || 0);
        });
        if (!list.length) {
            els.chatList.innerHTML = '<div class="list-empty">还没有会话，点击上方「新建会话」开始</div>';
            return;
        }
        var html = '';
        list.forEach(function (s) {
            var active = s.id === state.currentId ? ' active' : '';
            html += '<div class="chat-item' + active + '" data-id="' + escapeHtml(s.id) + '">' +
                '<span class="chat-item-icon">' + CHAT_ICON + '</span>' +
                '<span class="chat-item-title">' + escapeHtml(s.title || '新对话') + '</span>' +
                '<button class="chat-item-del" data-del="' + escapeHtml(s.id) + '" title="删除会话" aria-label="删除会话">×</button>' +
                '</div>';
        });
        els.chatList.innerHTML = html;
    }

    function showEmpty() {
        els.messages.innerHTML =
            '<div class="empty-state">' +
            '<div class="empty-logo">' + STAR_SVG + '</div>' +
            '<h1>有什么可以帮忙的？</h1>' +
            '<p>在下方输入消息，开始一段对话。</p>' +
            '</div>';
    }

    function showLoading() {
        els.messages.innerHTML =
            '<div class="loading-state"><span class="dots"><i></i><i></i><i></i></span>正在加载会话记录…</div>';
    }

    // 创建一条消息 DOM。streaming=true 时先显示跳动点。
    function addMessage(role, content, streaming) {
        var wrap = document.createElement('div');
        wrap.className = 'msg ' + (role === 'user' ? 'user' : 'assistant');

        if (role === 'user') {
            var bubble = document.createElement('div');
            bubble.className = 'bubble';
            bubble.innerHTML = escapeHtml(content || '').replace(/\n/g, '<br>');
            wrap.appendChild(bubble);
        } else {
            var logo = document.createElement('div');
            logo.className = 'assistant-logo';
            logo.innerHTML = STAR_SVG;

            var body = document.createElement('div');
            body.className = 'assistant-body';

            var md = document.createElement('div');
            md.className = 'md-content';
            md.innerHTML = window.MarkdownRender.render(content || '');
            body.appendChild(md);

            if (streaming) {
                var dots = document.createElement('span');
                dots.className = 'dots';
                dots.innerHTML = '<i></i><i></i><i></i>';
                body.appendChild(dots);
            }
            wrap.appendChild(logo);
            wrap.appendChild(body);
        }
        els.messages.appendChild(wrap);
        scrollBottom();
        return wrap;
    }

    function updateAssistant(text) {
        state.assistantText = text;
        if (!state.assistantNode) return;

        // 收到第一个字后，用闪烁光标替换跳动点
        if (!state.cursor) {
            var dots = state.assistantNode.querySelector('.dots');
            if (dots) dots.remove();
            var cursor = document.createElement('span');
            cursor.className = 'cursor';
            state.assistantNode.querySelector('.assistant-body').appendChild(cursor);
            state.cursor = cursor;
        }
        var md = state.assistantNode.querySelector('.md-content');
        if (md) md.innerHTML = window.MarkdownRender.render(text);
        scrollBottom();
    }

    function clearCursor() {
        if (state.cursor && state.cursor.parentNode) state.cursor.parentNode.removeChild(state.cursor);
        state.cursor = null;
    }

    function toggleSending(on) {
        els.sendBtn.hidden = on;
        els.stopBtn.hidden = !on;
        els.input.disabled = on;
        els.input.placeholder = on ? '正在生成…，可点击右侧停止' : '给 Claude 发送消息…';
        if (!on) els.input.focus();
    }

    function autoResize() {
        els.input.style.height = 'auto';
        els.input.style.height = Math.min(els.input.scrollHeight, 180) + 'px';
    }

    /* ---------------- 侧边栏 ---------------- */
    function openSidebar() {
        els.sidebar.classList.add('open');
        els.overlay.classList.add('show');
    }
    function closeSidebar() {
        els.sidebar.classList.remove('open');
        els.overlay.classList.remove('show');
    }

    /* ---------------- 会话操作 ---------------- */
    function createNewSession(activate) {
        var s = { id: genId(), title: '新对话', updatedAt: Date.now() };
        state.sessions.push(s);
        saveSessions();
        closeSidebar();
        if (activate !== false) {
            switchSession(s.id);
        } else {
            state.currentId = s.id;
            els.topbarTitle.textContent = s.title;
            renderSessions();
            showEmpty();
        }
        return s;
    }

    function switchSession(id) {
        if (state.streaming) { showError('请先等待当前回答生成完成，再切换会话。'); return; }
        if (state.currentId === id) { closeSidebar(); return; }
        state.currentId = id;
        var s = currentSession();
        els.topbarTitle.textContent = s ? (s.title || '新对话') : '新对话';
        renderSessions();
        closeSidebar();
        loadHistory(id);
    }

    function deleteSession(id) {
        if (state.streaming && state.currentId === id) {
            showError('生成中无法删除当前会话。');
            return;
        }
        // 调用后端清理记忆（失败也不影响本地移除）
        fetch(API.del + '?conversationId=' + encodeURIComponent(id)).catch(function () {});
        state.sessions = state.sessions.filter(function (s) { return s.id !== id; });
        saveSessions();
        if (state.currentId === id) {
            state.currentId = null;
            els.topbarTitle.textContent = '新对话';
            showEmpty();
        }
        renderSessions();
    }

    /* ---------------- 接口调用 ---------------- */
    function syncSessions() {
        fetch(API.list)
            .then(function (r) { return r.ok ? r.json() : null; })
            .then(function (data) {
                if (!Array.isArray(data)) return;
                var changed = false;
                data.forEach(function (item) {
                    var id = item.conversationId || item.id;
                    if (!id) return;
                    var exists = state.sessions.some(function (s) { return s.id === id; });
                    if (!exists) {
                        state.sessions.push({ id: id, title: item.title || '新对话', updatedAt: Date.now() });
                        changed = true;
                    }
                });
                if (changed) { saveSessions(); renderSessions(); }
            })
            .catch(function () { /* 后端未启动时仍可使用本地会话 */ });
    }

    function loadHistory(id) {
        showLoading();
        fetch(API.history + '?conversationId=' + encodeURIComponent(id))
            .then(function (r) {
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.json();
            })
            .then(function (data) {
                if (state.currentId !== id) return; // 期间已切换会话
                els.messages.innerHTML = '';
                if (!Array.isArray(data) || !data.length) { showEmpty(); return; }
                data.forEach(function (m) {
                    if (m.role === 'user' || m.role === 'assistant') {
                        addMessage(m.role, m.content || '', false);
                    }
                });
                if (!els.messages.querySelector('.msg')) showEmpty();
                scrollBottom();
            })
            .catch(function (err) {
                if (state.currentId !== id) return;
                showEmpty();
                showError('加载会话记录失败：' + err.message);
            });
    }

    function sendMessage() {
        var text = els.input.value.trim();
        if (!text || state.streaming) return;

        if (!state.currentId) createNewSession(false);

        els.input.value = '';
        autoResize();
        showError('');
        addMessage('user', text, false);

        // 首条消息作为会话标题
        var s = currentSession();
        if (s && (!s.title || s.title === '新对话')) {
            s.title = text.replace(/\s+/g, ' ').slice(0, 18) + (text.length > 18 ? '…' : '');
            s.updatedAt = Date.now();
            saveSessions();
            renderSessions();
        }
        els.topbarTitle.textContent = s ? s.title : '新对话';

        state.streaming = true;
        toggleSending(true);
        state.assistantNode = addMessage('assistant', '', true);
        state.assistantText = '';
        state.cursor = null;

        var controller = new AbortController();
        state.controller = controller;
        var url = API.stream + '?prompt=' + encodeURIComponent(text) +
            '&conversationId=' + encodeURIComponent(state.currentId);

        fetch(url, { signal: controller.signal })
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status + ' ' + (res.statusText || ''));
                if (!res.body) throw new Error('当前浏览器不支持流式读取');
                var reader = res.body.getReader();
                var decoder = new TextDecoder('utf-8');
                var buffer = '';

                function pump() {
                    return reader.read().then(function (result) {
                        if (result.done) {
                            buffer += decoder.decode();
                            updateAssistant(buffer);
                            return;
                        }
                        buffer += decoder.decode(result.value, { stream: true });
                        updateAssistant(buffer);
                        return pump();
                    });
                }
                return pump();
            })
            .catch(function (err) {
                if (err && err.name === 'AbortError') {
                    // 用户主动停止：保留已生成内容
                    return;
                }
                showError('发送失败：' + (err && err.message ? err.message : '网络异常') +
                    '。请确认后端服务（8085）与 Ollama（11434）已启动。');
                if (!state.assistantText) updateAssistant('抱歉，本次请求失败，请重试。');
            })
            .then(function () {
                state.streaming = false;
                state.controller = null;
                clearCursor();
                state.assistantNode = null;
                toggleSending(false);
                var cur = currentSession();
                if (cur) { cur.updatedAt = Date.now(); saveSessions(); renderSessions(); }
                scrollBottom();
            });
    }

    /* ---------------- 事件绑定 ---------------- */
    function init() {
        els.brandLogo.innerHTML = STAR_SVG;
        state.sessions = loadSessions();
        renderSessions();
        showEmpty();
        autoResize();

        els.newChatBtn.addEventListener('click', function () { createNewSession(true); });
        els.menuBtn.addEventListener('click', openSidebar);
        els.overlay.addEventListener('click', closeSidebar);
        els.sidebarClose.addEventListener('click', closeSidebar);

        els.chatList.addEventListener('click', function (e) {
            var del = e.target.closest ? e.target.closest('[data-del]') : null;
            if (del) { e.stopPropagation(); deleteSession(del.getAttribute('data-del')); return; }
            var item = e.target.closest ? e.target.closest('.chat-item') : null;
            if (item) switchSession(item.getAttribute('data-id'));
        });

        els.sendBtn.addEventListener('click', sendMessage);
        els.stopBtn.addEventListener('click', function () {
            if (state.controller) state.controller.abort();
        });

        els.input.addEventListener('input', autoResize);
        els.input.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) {
                e.preventDefault();
                sendMessage();
            }
        });

        els.errorClose.addEventListener('click', function () { showError(''); });

        syncSessions();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
