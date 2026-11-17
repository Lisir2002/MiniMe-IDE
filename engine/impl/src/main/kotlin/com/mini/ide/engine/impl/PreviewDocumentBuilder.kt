package com.mini.ide.engine.impl

import com.mini.ide.engine.api.PreviewDocument

/**
 * 把三文件内容组装成可渲染的完整 HTML 文档。纯逻辑，可 JVM 单测。
 *
 * 教学向约定：
 * - HTML 内容原样作为 body；若用户未提供完整 <html> 骨架则自动包裹；
 * - CSS 注入 <style>，JS 注入 <script defer> 放在 body 末尾；
 * - 角括号内容用文本原样嵌入（用户代码即内容，非来自不可信外部输入，属本地教学 IDE）。
 */
object PreviewDocumentBuilder {

    fun build(html: String, css: String, js: String, baseUrl: String): PreviewDocument {
        val document = buildString {
            if (!looksLikeFullDocument(html)) {
                append("<!DOCTYPE html><html><head><meta charset=\"utf-8\">")
                append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                append("</head><body>")
                append(html)
                if (css.isNotBlank()) append("\n<style>\n").append(css).append("\n</style>")
                if (js.isNotBlank()) append("\n<script>\n").append(js).append("\n</script>")
                append("</body></html>")
            } else {
                // 已是完整文档：在 </head> 前插 CSS，</body> 前插 JS
                append(injectIntoFullDocument(html, css, js))
            }
        }
        return PreviewDocument(html = document, css = css, js = js, baseUrl = baseUrl)
    }

    /** 完整文档时在 </head> 前注入 CSS、</body> 前注入 JS。 */
    private fun injectIntoFullDocument(html: String, css: String, js: String): String {
        val cssBlock = if (css.isBlank()) "" else "<style>\n$css\n</style>\n"
        val jsBlock = if (js.isBlank()) "" else "<script>\n$js\n</script>\n"
        val withCss = if (html.contains("</head>", ignoreCase = true)) {
            html.replaceFirst(Regex("</head>", RegexOption.IGNORE_CASE), "$cssBlock</head>")
        } else {
            cssBlock + html
        }
        return if (withCss.contains("</body>", ignoreCase = true)) {
            withCss.replaceFirst(Regex("</body>", RegexOption.IGNORE_CASE), "$jsBlock</body>")
        } else {
            withCss + jsBlock
        }
    }

    private fun looksLikeFullDocument(html: String): Boolean =
        html.contains("<html", ignoreCase = true) && html.contains("</html>", ignoreCase = true)
}
