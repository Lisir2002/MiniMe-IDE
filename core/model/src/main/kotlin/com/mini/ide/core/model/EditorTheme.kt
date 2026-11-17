package com.mini.ide.core.model

/**
 * 编辑器主题的**跨层纯数据契约**。
 *
 * 设计动机：视觉层（S2）用 Compose `Color`，但引擎层（S4）必须能在纯 JVM 上单测，
 * 不能依赖 Compose/Android UI。因此双方约定用 ARGB Long（0xAARRGGBB）作为传输形态：
 * - designsystem 负责 `Color -> Long` 转换；
 * - 桥接引擎把 Long 序列化成 CSS 变量 JSON 注入 CodeMirror。
 *
 * 字段与视觉规范 `EditorTokens` 一一对应。
 */
data class EditorTheme(
    val background: Long,
    val foreground: Long,
    val gutter: Long,
    val selection: Long,
    val cursor: Long,
    val syntaxKeyword: Long,
    val syntaxString: Long,
    val syntaxNumber: Long,
    val syntaxComment: Long,
    val syntaxTag: Long,
    val syntaxAttribute: Long,
) {
    companion object {
        /** 默认暗色，供引擎单测与未注入主题时兜底。 */
        val DEFAULT_DARK = EditorTheme(
            background = 0xFF000000,
            foreground = 0xFFECECF2,
            gutter = 0xFF1C1C1F,
            selection = 0x400A84FF,
            cursor = 0xFF0A84FF,
            syntaxKeyword = 0xFFFF7B72,
            syntaxString = 0xFFA5D6FF,
            syntaxNumber = 0xFF79C0FF,
            syntaxComment = 0xFF6E6E7A,
            syntaxTag = 0xFF7EE787,
            syntaxAttribute = 0xFFD2A8FF,
        )
    }
}
