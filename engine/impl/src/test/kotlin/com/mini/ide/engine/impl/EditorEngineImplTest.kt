package com.mini.ide.engine.impl

import com.mini.ide.core.model.EditorTheme
import com.mini.ide.engine.api.BridgeMessage
import com.mini.ide.engine.api.BridgeMethod
import com.mini.ide.engine.api.EditorEvent
import com.mini.ide.testdouble.FakeBridgeEngine
import com.mini.ide.testdouble.FakeLanguageProfile
import com.mini.ide.testdouble.FakeWebViewPort
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorEngineImplTest {

    @Test
    fun `load_下发SET_CONTENT与SET_LANGUAGE并建立内容镜像`() {
        val bridge = FakeBridgeEngine()
        val engine = EditorEngineImpl(bridge)
        engine.load("f1", "<h1>hi</h1>", FakeLanguageProfile)
        assertEquals("<h1>hi</h1>", engine.currentContent())
        val methods = bridge.calls.map { it.first }
        assertTrue(methods.contains(BridgeMethod.SET_CONTENT))
        assertTrue(methods.contains(BridgeMethod.SET_LANGUAGE))
    }

    @Test
    fun `内容变化消息_更新镜像并发射ContentChanged事件`() = runBlocking {
        val bridge = FakeBridgeEngine()
        val engine = EditorEngineImpl(bridge)
        engine.load("f1", "old", FakeLanguageProfile)

        // SharedFlow replay=0：必须先订阅再触发，否则事件丢失
        val deferred = async { engine.events.first() { it is EditorEvent.ContentChanged } }
        yield()
        engine.onMessage(BridgeMessage.CONTENT_CHANGED, BridgeCodec.encodeText("new content"))

        assertEquals("new content", engine.currentContent())
        val event = deferred.await() as EditorEvent.ContentChanged
        assertEquals("f1", event.fileId)
        assertEquals("new content", event.content)
    }

    @Test
    fun `光标消息_解析行列并发射CursorMoved`() = runBlocking {
        val engine = EditorEngineImpl(FakeBridgeEngine())
        val deferred = async { engine.events.first() { it is EditorEvent.CursorMoved } }
        yield()
        engine.onMessage(BridgeMessage.CURSOR_MOVED, "12:34")
        val event = deferred.await() as EditorEvent.CursorMoved
        assertEquals(12, event.line)
        assertEquals(34, event.column)
    }

    @Test
    fun `非法光标payload_忽略不崩溃`() {
        val engine = EditorEngineImpl(FakeBridgeEngine())
        engine.onMessage(BridgeMessage.CURSOR_MOVED, "garbage")
        assertTrue(true)
    }

    @Test
    fun `undo与redo_下发正确方法`() {
        val bridge = FakeBridgeEngine()
        val engine = EditorEngineImpl(bridge)
        engine.undo()
        engine.redo()
        val methods = bridge.calls.map { it.first }
        assertTrue(methods.contains(BridgeMethod.UNDO))
        assertTrue(methods.contains(BridgeMethod.REDO))
    }

    @Test
    fun `setTheme_下发序列化的主题JSON`() {
        val bridge = FakeBridgeEngine()
        val engine = EditorEngineImpl(bridge)
        engine.setTheme(EditorTheme.DEFAULT_DARK)
        val themeCall = bridge.calls.first { it.first == BridgeMethod.SET_THEME }
        assertTrue((themeCall.second[0] as String).contains("--cm-bg"))
    }

    @Test
    fun `bind_下发语言模式`() {
        val bridge = FakeBridgeEngine()
        val engine = EditorEngineImpl(bridge)
        engine.bind(FakeWebViewPort(), FakeLanguageProfile, EditorTheme.DEFAULT_DARK)
        assertTrue(bridge.calls.any { it.first == BridgeMethod.SET_LANGUAGE })
    }
}
