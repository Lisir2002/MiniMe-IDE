package com.mini.ide.engine.impl

import com.mini.ide.core.model.EditorTheme
import org.json.JSONObject

/**
 * 编辑器主题序列化：ARGB Long → CSS 变量 JSON，供桥接引擎注入 CodeMirror。
 * 纯逻辑，可 JVM 单测。
 */
object ThemeSerializer {

    /** 把 ARGB Long 转成 #AARRGGBB（CodeMirror 接受的 CSS 颜色格式）。 */
    fun argbToCss(argb: Long): String = buildString {
        append('#')
        append(((argb shr 24) and 0xFF).toInt().hex())
        append(((argb shr 16) and 0xFF).toInt().hex())
        append(((argb shr 8) and 0xFF).toInt().hex())
        append((argb and 0xFF).toInt().hex())
    }

    private fun Int.hex(): String = this.toString(16).padStart(2, '0').uppercase()

    /** 输出 { "--cm-bg": "#...", ... }，键名与 CodeMirror 主题 CSS 变量约定一致。 */
    fun toJson(theme: EditorTheme): String = JSONObject().apply {
        put("--cm-bg", argbToCss(theme.background))
        put("--cm-fg", argbToCss(theme.foreground))
        put("--cm-gutter", argbToCss(theme.gutter))
        put("--cm-selection", argbToCss(theme.selection))
        put("--cm-cursor", argbToCss(theme.cursor))
        put("--cm-keyword", argbToCss(theme.syntaxKeyword))
        put("--cm-string", argbToCss(theme.syntaxString))
        put("--cm-number", argbToCss(theme.syntaxNumber))
        put("--cm-comment", argbToCss(theme.syntaxComment))
        put("--cm-tag", argbToCss(theme.syntaxTag))
        put("--cm-attribute", argbToCss(theme.syntaxAttribute))
    }.toString()
}
