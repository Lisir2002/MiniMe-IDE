package com.mini.ide.language.dsl

/**
 * MiniLang 抽象语法树（AST）。
 *
 * 教学向 v1：仅支持"一条语句一行"，元素可带属性，卡片可含子语句块。
 * 全部为纯 data class，零 Android 依赖，便于穷举单测。
 */

sealed interface DslStatement {
    val line: Int
}

/** 页面声明：`页面 "标题"`。 */
data class PageStatement(val title: String, override val line: Int) : DslStatement

enum class ElementType { TEXT, HEADING, BUTTON, CARD }

/**
 * 元素语句：`文字 "…"` / `标题 "…"` / `按钮 "…"` / `卡片 "…" { … }`。
 * [attributes] 为 `键 "值"` 形式（如 颜色/大小）；[children] 仅卡片容器使用。
 */
data class ElementStatement(
    val type: ElementType,
    val text: String,
    val attributes: Map<String, String> = emptyMap(),
    val children: List<DslStatement> = emptyList(),
    override val line: Int,
) : DslStatement

/** 点击事件：`当点击 "按钮文本" 显示 "提示"`。 */
data class ClickStatement(
    val buttonText: String,
    val message: String,
    override val line: Int,
) : DslStatement

/** 全局样式：`设置 背景 "颜色"`。 */
data class SettingStatement(
    val key: String,
    val value: String,
    override val line: Int,
) : DslStatement
