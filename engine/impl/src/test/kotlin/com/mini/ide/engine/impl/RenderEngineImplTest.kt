package com.mini.ide.engine.impl

import com.mini.ide.engine.api.BridgeMessage
import com.mini.ide.engine.api.LogLevel
import com.mini.ide.engine.api.PreviewDocument
import com.mini.ide.engine.api.RenderEvent
import com.mini.ide.testdouble.FakeWebViewPort
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RenderEngineImplTest {

    private fun doc() = PreviewDocument("<html></html>", ".a{}", "var a=1", "http://base/")

    @Test
    fun `render_把HTML加载进WebView并发出Completed`() = runBlocking {
        val port = FakeWebViewPort()
        val engine = RenderEngineImpl()
        engine.bind(port)
        val deferred = async { engine.events.first() { it is RenderEvent.Completed } }
        yield()
        engine.render(doc())
        assertEquals("http://base/", port.loadedBaseUrl)
        assertTrue(port.loadedData!!.contains("<html></html>"))
        val event = deferred.await() as RenderEvent.Completed
        assertTrue(event.durationMs >= 0)
    }

    @Test
    fun `reload_调用WebView reload`() {
        val port = FakeWebViewPort()
        val engine = RenderEngineImpl()
        engine.bind(port)
        engine.reload()
        assertEquals(1, port.reloadCount)
    }

    @Test
    fun `console消息_解析级别行号与内容`() = runBlocking {
        val engine = RenderEngineImpl()
        val deferred = async { engine.events.first() { it is RenderEvent.Console } }
        yield()
        engine.onMessage(BridgeMessage.CONSOLE, "WARN|42|小心")
        val event = deferred.await() as RenderEvent.Console
        assertEquals(LogLevel.WARN, event.level)
        assertEquals(42, event.line)
        assertEquals("小心", event.message)
    }

    @Test
    fun `运行时错误消息_解析行列与信息`() = runBlocking {
        val engine = RenderEngineImpl()
        val deferred = async { engine.events.first() { it is RenderEvent.RuntimeError } }
        yield()
        engine.onMessage(BridgeMessage.RUNTIME_ERROR, "7|main.js|TypeError: x")
        val event = deferred.await() as RenderEvent.RuntimeError
        assertEquals(7, event.line)
        assertEquals("main.js", event.source)
        assertEquals("TypeError: x", event.message)
    }

    @Test
    fun `非法console payload_忽略不崩溃`() {
        val engine = RenderEngineImpl()
        engine.onMessage(BridgeMessage.CONSOLE, "bad")
        assertTrue(true)
    }
}
