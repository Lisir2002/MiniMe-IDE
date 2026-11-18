package com.mini.ide.language.dsl

/**
 * MiniLang 代码生成器：AST → HTML / CSS / JS 三段。
 *
 * 教学向规则：
 * - 文本一律 HTML 转义，杜绝本地示例代码破坏预览结构；
 * - 颜色采用闭集词表映射，`#RRGGBB` 原样通过；
 * - 点击事件生成带行号注解的 JS，点击按钮弹出提示（用 `alert`，零依赖）；
 * - 页面标题映射为 `<title>` + 首屏 `<h1>`。
 *
 * 纯函数，可穷举单测。
 */
object DslCodeGenerator {

    private val colorMap = mapOf(
        "红" to "#FF453A",
        "绿" to "#30D158",
        "蓝" to "#0A84FF",
        "深蓝" to "#1C3A70",
        "浅灰" to "#E5E5EA",
        "白" to "#FFFFFF",
        "黑" to "#000000",
    )

    fun generate(statements: List<DslStatement>): Generated {
        val body = StringBuilder()
        val styles = StringBuilder()
        val scripts = StringBuilder()
        var pageTitle: String? = null
        var background: String? = null
        var clickIndex = 0

        // 先找出页面标题与背景设置（全局指令，与出现顺序无关）
        statements.filterIsInstance<PageStatement>().firstOrNull()?.let { pageTitle = it.title }
        statements.filterIsInstance<SettingStatement>().firstOrNull { it.key == "背景" }?.let { background = resolveColor(it.value) }

        // 收集所有按钮的文本 → 用于点击事件有效性提示
        val buttonTexts = mutableSetOf<String>()
        fun collectButtons(list: List<DslStatement>) {
            list.forEach { s ->
                if (s is ElementStatement && s.type == ElementType.BUTTON) buttonTexts += s.text
                if (s is ElementStatement) collectButtons(s.children)
            }
        }
        collectButtons(statements)

        // 点击事件：生成 JS，并对"点击了不存在的按钮"给出行号诊断（通过返回值）
        val warnings = mutableListOf<Pair<Int, String>>()
        statements.filterIsInstance<ClickStatement>().forEach { click ->
            if (click.buttonText !in buttonTexts) {
                warnings += click.line to "点击事件指向的按钮「${click.buttonText}」不存在"
            }
            clickIndex++
            scripts.append("document.querySelectorAll('button').forEach(function(b){")
                .append("if(b.textContent.trim()===\"").append(jsEscape(click.buttonText)).append("\"){")
                .append("b.addEventListener('click',function(){alert(\"").append(jsEscape(click.message)).append("\");});")
                .append("}});\n")
        }

        // 元素渲染
        fun render(list: List<DslStatement>) {
            list.forEach { s ->
                when (s) {
                    is PageStatement -> {
                        body.append("<h1 class=\"minilang-title\">").append(escapeHtml(s.title)).append("</h1>\n")
                    }
                    is ElementStatement -> {
                        val style = buildInlineStyle(s.attributes, isCard = s.type == ElementType.CARD)
                        val classAttr = when (s.type) {
                            ElementType.TEXT -> "minilang-text"
                            ElementType.HEADING -> "minilang-heading"
                            ElementType.BUTTON -> "minilang-button"
                            ElementType.CARD -> "minilang-card"
                        }
                        val styleAttr = if (style.isBlank()) "" else " style=\"$style\""
                        when (s.type) {
                            ElementType.CARD -> {
                                body.append("<div class=\"").append(classAttr).append("\"").append(styleAttr).append(">\n")
                                body.append("<div class=\"minilang-card-title\">").append(escapeHtml(s.text)).append("</div>\n")
                                render(s.children)
                                body.append("</div>\n")
                            }
                            ElementType.BUTTON -> body.append("<button class=\"").append(classAttr).append("\"").append(styleAttr)
                                .append(">").append(escapeHtml(s.text)).append("</button>\n")
                            ElementType.HEADING -> body.append("<h2 class=\"").append(classAttr).append("\"").append(styleAttr)
                                .append(">").append(escapeHtml(s.text)).append("</h2>\n")
                            ElementType.TEXT -> body.append("<p class=\"").append(classAttr).append("\"").append(styleAttr)
                                .append(">").append(escapeHtml(s.text)).append("</p>\n")
                        }
                    }
                    is SettingStatement, is ClickStatement -> Unit // 已在上面处理
                }
            }
        }
        render(statements)

        // 基础样式，保证无 CSS 输入时也有可读排版；背景设置覆盖 body
        styles.append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;")
            .append("margin:0;padding:24px;line-height:1.6;")
            .append(background?.let { "background:$it;" }.orEmpty())
            .append("}\n")
            .append(".minilang-title{font-size:26px;font-weight:700;margin:0 0 12px;}\n")
            .append(".minilang-heading{font-size:20px;font-weight:600;margin:12px 0 6px;}\n")
            .append(".minilang-text{margin:6px 0;}\n")
            .append(".minilang-card{border-radius:14px;padding:14px 16px;margin:12px 0;")
            .append("background:rgba(120,120,128,.12);}\n")
            .append(".minilang-card-title{font-weight:600;margin-bottom:6px;}\n")
            .append(".minilang-button{border:0;border-radius:12px;padding:10px 18px;cursor:pointer;")
            .append("background:#0A84FF;color:#fff;font-size:15px;margin:6px 6px 6px 0;}\n")

        // 输出"骨架 HTML"（不含内联 style/script），css/js 独立返回，
        // 交由 M4 PreviewDocumentBuilder 统一注入，与手写三文件走同一渲染链路。
        val html = buildString {
            append("<!DOCTYPE html><html lang=\"zh\"><head><meta charset=\"utf-8\">")
            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
            val safeTitle = (pageTitle ?: "MiniLang 页面").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
            append("<title>").append(safeTitle).append("</title></head><body>\n")
            append(body)
            append("</body></html>")
        }

        return Generated(
            html = html,
            css = styles.toString(),
            js = scripts.toString(),
            warnings = warnings,
        )
    }

    private fun buildInlineStyle(attrs: Map<String, String>, isCard: Boolean): String {
        val parts = mutableListOf<String>()
        attrs["颜色"]?.let { parts += "color:${resolveColor(it)}" }
        attrs["大小"]?.let { parts += "font-size:${resolveSize(it)}" }
        return parts.joinToString(";")
    }

    private fun resolveColor(value: String): String =
        if (value.startsWith("#")) value.uppercase() else colorMap[value] ?: value

    private fun resolveSize(value: String): String = when (value) {
        "大" -> "24px"
        "小" -> "14px"
        else -> "18px"
    }

    private fun escapeHtml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun jsEscape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")

    data class Generated(
        val html: String,
        val css: String,
        val js: String,
        val warnings: List<Pair<Int, String>>,
    )
}
