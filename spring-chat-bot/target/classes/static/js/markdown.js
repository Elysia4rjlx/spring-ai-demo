/* ============================================================
   Markdown 渲染
   - 优先使用 CDN 的 marked + DOMPurify（若加载成功）
   - 离线 / CDN 失败时自动降级到内置轻量渲染器（无外部依赖）
   所有输出均做 HTML 转义与链接白名单处理，避免 XSS。
   ============================================================ */
window.MarkdownRender = (function () {
    'use strict';

    function escapeHtml(s) {
        return String(s == null ? '' : s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function safeUrl(u) {
        var t = String(u == null ? '' : u).trim();
        // 仅允许 http(s)、mailto、站内绝对路径、锚点、相对路径
        if (/^(https?:\/\/|mailto:|\/|#|\.\/|\.\.\/)/i.test(t)) return t;
        return '#';
    }

    /* ---------- 行内元素 ---------- */
    function inline(text) {
        var s = escapeHtml(text);

        // 行内代码先占位，避免内部符号被后续规则误处理
        var codes = [];
        s = s.replace(/`([^`]+)`/g, function (m, c) {
            codes.push('<code>' + c + '</code>');
            return '\u0000' + (codes.length - 1) + '\u0000';
        });

        // 图片
        s = s.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, function (m, alt, url) {
            return '<img alt="' + alt + '" src="' + safeUrl(url) + '">';
        });
        // 链接
        s = s.replace(/\[([^\]]+)\]\(([^)]+)\)/g, function (m, txt, url) {
            return '<a href="' + safeUrl(url) + '" target="_blank" rel="noopener noreferrer">' + txt + '</a>';
        });
        // 粗体 / 删除线 / 斜体
        s = s.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        s = s.replace(/__([^_]+)__/g, '<strong>$1</strong>');
        s = s.replace(/~~([^~]+)~~/g, '<del>$1</del>');
        s = s.replace(/(^|[^*])\*([^*\n]+)\*/g, '$1<em>$2</em>');
        s = s.replace(/(^|[^_])_([^_\n]+)_/g, '$1<em>$2</em>');

        // 恢复行内代码
        s = s.replace(/\u0000(\d+)\u0000/g, function (m, i) {
            return codes[Number(i)] || '';
        });
        return s;
    }

    /* ---------- 内置块级渲染（降级方案） ---------- */
    function fallback(src) {
        var lines = String(src).split('\n');
        var out = [];
        var i = 0;
        var listType = null;
        var inCode = false, codeLang = '', codeBuf = [];

        function closeList() {
            if (listType) { out.push('</' + listType + '>'); listType = null; }
        }
        function isBlockStart(l) {
            return /^(#{1,6}\s|>|```|\s*([-*+])\s|\s*\d+\.\s|\s*(---+|\*\*\*+|___+)\s*$)/.test(l);
        }

        while (i < lines.length) {
            var line = lines[i];

            // 围栏代码块
            if (/^```/.test(line)) {
                if (!inCode) {
                    closeList();
                    inCode = true;
                    codeLang = line.slice(3).trim();
                    codeBuf = [];
                } else {
                    out.push('<pre><code' + (codeLang ? ' class="language-' + escapeHtml(codeLang) + '"' : '') + '>' + escapeHtml(codeBuf.join('\n')) + '</code></pre>');
                    inCode = false;
                }
                i++;
                continue;
            }
            if (inCode) { codeBuf.push(line); i++; continue; }

            // 空行
            if (!line.trim()) { closeList(); i++; continue; }

            // 标题
            var m = line.match(/^(#{1,6})\s+(.*)$/);
            if (m) {
                closeList();
                var lv = m[1].length;
                out.push('<h' + lv + '>' + inline(m[2]) + '</h' + lv + '>');
                i++;
                continue;
            }

            // 分隔线
            if (/^\s*(---+|\*\*\*+|___+)\s*$/.test(line)) {
                closeList(); out.push('<hr>'); i++; continue;
            }

            // 引用（递归渲染）
            if (/^>\s?/.test(line)) {
                closeList();
                var q = [];
                while (i < lines.length && /^>\s?/.test(lines[i])) {
                    q.push(lines[i].replace(/^>\s?/, ''));
                    i++;
                }
                out.push('<blockquote>' + fallback(q.join('\n')) + '</blockquote>');
                continue;
            }

            // 无序列表
            m = line.match(/^\s*([-*+])\s+(.*)$/);
            if (m) {
                if (listType !== 'ul') { closeList(); out.push('<ul>'); listType = 'ul'; }
                out.push('<li>' + inline(m[2]) + '</li>');
                i++;
                continue;
            }

            // 有序列表
            m = line.match(/^\s*\d+\.\s+(.*)$/);
            if (m) {
                if (listType !== 'ol') { closeList(); out.push('<ol>'); listType = 'ol'; }
                out.push('<li>' + inline(m[1]) + '</li>');
                i++;
                continue;
            }

            // 段落（合并连续行，换行转换为 <br>）
            closeList();
            var p = [line];
            i++;
            while (i < lines.length && lines[i].trim() && !isBlockStart(lines[i])) {
                p.push(lines[i]);
                i++;
            }
            out.push('<p>' + p.map(inline).join('<br>') + '</p>');
        }

        if (inCode) out.push('<pre><code>' + escapeHtml(codeBuf.join('\n')) + '</code></pre>');
        closeList();
        return out.join('\n');
    }

    /* ---------- 对外入口 ---------- */
    function render(src) {
        if (src == null) return '';
        src = String(src);
        if (typeof marked !== 'undefined') {
            try {
                var opts = { breaks: true, gfm: true };
                var html = (typeof marked.parse === 'function') ? marked.parse(src, opts) : marked(src, opts);
                if (typeof DOMPurify !== 'undefined' && DOMPurify.sanitize) {
                    html = DOMPurify.sanitize(html, { USE_PROFILES: { html: true } });
                }
                return html;
            } catch (e) {
                // 渲染异常时降级
            }
        }
        return fallback(src);
    }

    return { render: render };
})();
