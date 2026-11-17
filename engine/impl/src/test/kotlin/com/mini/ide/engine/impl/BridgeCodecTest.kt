package com.mini.ide.engine.impl

import com.mini.ide.engine.api.BridgeMessage
import com.mini.ide.engine.api.BridgeMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BridgeCodecTest {

    @Test
    fun `文本 Base64 往返_含中文与特殊字符`() {
        val original = "<div>你好 \"world\" & 'x'\n第二行</div>"
        val encoded = BridgeCodec.encodeText(original)
        assertEquals(original, BridgeCodec.decodeText(encoded))
        // Base64 输出不含换行
        assertTrue(encoded.none { it == '\n' || it == '\r' })
    }

    @Test
    fun `encodeCall_生成合法dispatch语句且方法名正确`() {
        val script = BridgeCodec.encodeCall(BridgeMethod.SET_CONTENT, listOf("YWJj"))
        assertTrue(script.startsWith("window.__ide.dispatch("))
        assertTrue(script.contains("\"method\":\"SET_CONTENT\""))
        assertTrue(script.contains("\"args\":[\"YWJj\"]"))
        assertTrue(script.endsWith(")"))
    }

    @Test
    fun `encodeCall_多参数与数值参数`() {
        val script = BridgeCodec.encodeCall(BridgeMethod.SET_FONT_SIZE, listOf(16))
        assertTrue(script.contains("SET_FONT_SIZE"))
        assertTrue(script.contains("[16]"))
    }

    @Test
    fun `decodeMessage_解析CONTENT_CHANGED`() {
        val decoded = BridgeCodec.decodeMessage("{\"type\":\"CONTENT_CHANGED\",\"payload\":\"YWJj\"}")
        assertEquals(BridgeMessage.CONTENT_CHANGED, decoded.type)
        assertEquals("YWJj", decoded.payload)
    }

    @Test
    fun `decodeMessage_解析CURSOR_MOVED并保留payload`() {
        val decoded = BridgeCodec.decodeMessage("{\"type\":\"CURSOR_MOVED\",\"payload\":\"3:7\"}")
        assertEquals(BridgeMessage.CURSOR_MOVED, decoded.type)
        assertEquals("3:7", decoded.payload)
    }

    @Test
    fun `decodeMessage_未知类型抛异常`() {
        try {
            BridgeCodec.decodeMessage("{\"type\":\"NO_SUCH_TYPE\"}")
            org.junit.Assert.fail("应当抛出 IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("unknown bridge message"))
        }
    }

    @Test
    fun `decodeMessage_缺失payload默认为空串`() {
        val decoded = BridgeCodec.decodeMessage("{\"type\":\"READY\"}")
        assertEquals(BridgeMessage.READY, decoded.type)
        assertEquals("", decoded.payload)
    }
}
