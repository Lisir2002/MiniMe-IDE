package com.mini.ide.engine.impl

import com.mini.ide.engine.api.BridgeMessage
import com.mini.ide.engine.api.BridgeMethod
import org.json.JSONArray
import org.json.JSONObject
import java.util.Base64

/**
 * 桥接协议编解码器 —— 纯函数式，是 JS↔Kotlin 通信的**唯一序列化真源**。
 *
 * 协议：
 * - Kotlin→JS：`window.__ide.dispatch({ method, args })`，文本类参数用 Base64。
 * - JS→Kotlin：`{ type, payload }`，CONTENT_CHANGED 的 payload 为 Base64。
 *
 * 依赖 `org.json`：Android 平台自带的 org.json 在纯 JVM 单测里是 stub，
 * 故 engine:impl 显式依赖 org.json:json 真实实现，使本类可在 JVM 单测。
 */
object BridgeCodec {

    /** 把方法 + 参数编码成 dispatch 调用语句。 */
    fun encodeCall(method: BridgeMethod, args: List<Any>): String {
        val json = JSONObject().apply {
            put("method", method.name)
            put("args", JSONArray(args))
        }
        return "window.__ide.dispatch($json)"
    }

    /** 解析 JS 回传的 JSON。非法 JSON 抛 IllegalArgumentException，由上层兜底。 */
    fun decodeMessage(json: String): DecodedMessage {
        val obj = JSONObject(json)
        val type = runCatching { BridgeMessage.valueOf(obj.getString("type")) }
            .getOrElse { throw IllegalArgumentException("unknown bridge message type: $json") }
        val payload = obj.optString("payload", "")
        return DecodedMessage(type, payload)
    }

    /** 文本 → Base64（标准编码，JS 侧 atob 兼容）。 */
    fun encodeText(text: String): String =
        Base64.getEncoder().encodeToString(text.toByteArray(Charsets.UTF_8))

    /** Base64 → 文本。 */
    fun decodeText(base64: String): String =
        String(Base64.getDecoder().decode(base64), Charsets.UTF_8)

    data class DecodedMessage(val type: BridgeMessage, val payload: String)
}
