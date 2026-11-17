package com.mini.ide.feature.preview

import com.mini.ide.engine.api.PreviewDocument
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewBridgeScriptTest {

    private val doc = PreviewDocument(
        html = "<!DOCTYPE html><html><head><title>T</title></head><body><h1>Hi</h1></body></html>",
        css = "", js = "", baseUrl = "file:///android_asset/preview/",
    )

    @Test
    fun `wrap injects script right after head`() {
        val wrapped = PreviewBridgeScript.wrap(doc)
        val headIdx = wrapped.html.indexOf("<head>")
        val scriptIdx = wrapped.html.indexOf("<script>", startIndex = headIdx)
        assertTrue(scriptIdx > headIdx)
        assertTrue(wrapped.html.contains("AndroidBridge.post"))
        assertTrue(wrapped.html.contains("RUNTIME_ERROR"))
        assertTrue(wrapped.html.contains("CONSOLE"))
        // 原有内容保留
        assertTrue(wrapped.html.contains("<h1>Hi</h1>"))
    }

    @Test
    fun `wrap handles document without head`() {
        val noHead = doc.copy(html = "<html><body>x</body></html>")
        val wrapped = PreviewBridgeScript.wrap(noHead)
        assertTrue(wrapped.html.contains("<script>"))
        assertTrue(wrapped.html.startsWith("<html>"))
    }

    @Test
    fun `wrap handles fragment`() {
        val fragment = doc.copy(html = "<h1>fragment</h1>")
        val wrapped = PreviewBridgeScript.wrap(fragment)
        assertTrue(wrapped.html.startsWith(PreviewBridgeScript.SCRIPT))
        assertTrue(wrapped.html.contains("<h1>fragment</h1>"))
    }
}
