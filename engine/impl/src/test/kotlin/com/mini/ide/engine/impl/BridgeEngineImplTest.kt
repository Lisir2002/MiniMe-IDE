package com.mini.ide.engine.impl

import com.mini.ide.engine.api.BridgeEngine
import com.mini.ide.engine.api.BridgeHandler
import com.mini.ide.engine.api.BridgeMessage
import com.mini.ide.engine.api.BridgeMethod
import com.mini.ide.testdouble.FakeWebViewPort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BridgeEngineImplTest {

    @Test
    fun `install_注入名为AndroidBridge的JS接口`() {
        val port = FakeWebViewPort()
        val bridge: BridgeEngine = BridgeEngineImpl()
        val handler = BridgeHandler { _, _ -> }
        bridge.install(port, handler)
        assertEquals("AndroidBridge", port.jsInterfaceName)
        assertTrue(port.jsInterface is BridgeEngineImpl.JsBridge)
    }

    @Test
    fun `call_经统一出口产生dispatch脚本`() {
        val port = FakeWebViewPort()
        val bridge = BridgeEngineImpl()
        bridge.install(port, BridgeHandler { _, _ -> })
        bridge.call(BridgeMethod.SET_FONT_SIZE, 18)
        assertEquals(1, port.javascripts.size)
        assertTrue(port.javascripts[0].contains("SET_FONT_SIZE"))
        assertTrue(port.javascripts[0].contains("[18]"))
    }

    @Test
    fun `receive_解析消息并分发给handler`() {
        val bridge = BridgeEngineImpl()
        var gotType: BridgeMessage? = null
        var gotPayload: String? = null
        bridge.install(FakeWebViewPort(), BridgeHandler { type, payload ->
            gotType = type
            gotPayload = payload
        })
        bridge.receive("{\"type\":\"CONTENT_CHANGED\",\"payload\":\"aGVsbG8=\"}")
        assertEquals(BridgeMessage.CONTENT_CHANGED, gotType)
        assertEquals("aGVsbG8=", gotPayload)
    }

    @Test
    fun `receive_非法JSON不崩溃且不分发`() {
        val bridge = BridgeEngineImpl()
        var called = false
        bridge.install(FakeWebViewPort(), BridgeHandler { _, _ -> called = true })
        bridge.receive("not-a-json")
        assertTrue(!called)
    }

    @Test
    fun `JsBridge_post回调转到receive`() {
        val bridge = BridgeEngineImpl()
        var gotType: BridgeMessage? = null
        bridge.install(FakeWebViewPort(), BridgeHandler { type, _ -> gotType = type })
        BridgeEngineImpl.JsBridge(bridge).post("{\"type\":\"READY\"}")
        assertEquals(BridgeMessage.READY, gotType)
    }
}
