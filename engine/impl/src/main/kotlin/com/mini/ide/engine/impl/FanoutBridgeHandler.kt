package com.mini.ide.engine.impl

import com.mini.ide.engine.api.BridgeHandler
import com.mini.ide.engine.api.BridgeMessage

/** 把单一桥接入口的消息扇出给多个处理器（编辑引擎 + 渲染引擎）。 */
class FanoutBridgeHandler(
    private val handlers: List<BridgeHandler>,
) : BridgeHandler {
    override fun onMessage(type: BridgeMessage, payload: String) {
        handlers.forEach { it.onMessage(type, payload) }
    }
}
