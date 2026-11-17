package com.mini.ide.engine.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewDocumentBuilderTest {

    @Test
    fun `片段HTML_自动包裹完整文档并注入CSS与JS`() {
        val doc = PreviewDocumentBuilder.build(
            html = "<h1>Hi</h1>",
            css = "h1{color:red}",
            js = "console.log(1)",
            baseUrl = "http://localhost/",
        )
        assertTrue(doc.html.startsWith("<!DOCTYPE html>"))
        assertTrue(doc.html.contains("<h1>Hi</h1>"))
        assertTrue(doc.html.contains("<style>"))
        assertTrue(doc.html.contains("h1{color:red}"))
        assertTrue(doc.html.contains("<script>"))
        assertTrue(doc.html.contains("console.log(1)"))
        assertTrue(doc.html.contains("</body></html>"))
        assertEquals("http://localhost/", doc.baseUrl)
    }

    @Test
    fun `完整HTML_CSS注入head前_JS注入body前`() {
        val html = "<!DOCTYPE html><html><head><title>T</title></head><body><p>x</p></body></html>"
        val doc = PreviewDocumentBuilder.build(html, "body{}", "alert(1)", "about:blank")
        assertTrue(doc.html.indexOf("<style>") < doc.html.indexOf("</head>"))
        assertTrue(doc.html.indexOf("<script>") < doc.html.indexOf("</body>"))
        assertTrue(doc.html.contains("<title>T</title>"))
        // 只包裹一次，不应再出现第二个 DOCTYPE
        assertEquals(1, Regex("<!DOCTYPE", RegexOption.IGNORE_CASE).findAll(doc.html).count())
    }

    @Test
    fun `空CSS与JS_不注入空标签`() {
        val doc = PreviewDocumentBuilder.build("<p>x</p>", "", "", "base")
        assertTrue(!doc.html.contains("<style>"))
        assertTrue(!doc.html.contains("<script>"))
    }

    @Test
    fun `原样保留PreviewDocument中的三文件字段`() {
        val doc = PreviewDocumentBuilder.build("<b>x</b>", ".a{}", "var a=1;", "base")
        assertTrue(doc.html.contains("<b>x</b>"))
        assertEquals(".a{}", doc.css)
        assertEquals("var a=1;", doc.js)
    }
}
