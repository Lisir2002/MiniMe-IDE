package com.mini.ide.language.dsl

import com.mini.ide.language.api.BracketPair
import com.mini.ide.language.api.CompletionItem
import com.mini.ide.language.api.CompletionKind
import com.mini.ide.language.api.HighlightSpec
import com.mini.ide.language.api.LanguageId
import com.mini.ide.language.api.LanguageProfile
import com.mini.ide.language.api.Snippet
import com.mini.ide.language.api.SnippetScope

/**
 * MiniLang 语言档案：数据驱动的可插拔单元。
 *
 * 注册进 LanguageRegistry 后，编辑器内核零改动即获得中文关键字高亮、补全与括号配对。
 * 这是 M2 预留可插拔契约在 Phase3 的兑现——证明"新增一门语言 = 新增一个 Profile"。
 */
object DslProfile : LanguageProfile {
    override val id = LanguageId.DSL
    override val displayName = "MiniLang"
    override val fileExtensions = listOf("dsl", "mini")
    override val codemirrorMode = "text/x-minilang"

    override val highlight = HighlightSpec(
        keywords = setOf("页面", "文字", "标题", "按钮", "卡片", "当点击", "显示", "设置", "背景", "颜色", "大小"),
        builtins = setOf("红", "绿", "蓝", "深蓝", "浅灰", "白", "黑", "大", "小"),
        commentPrefix = "#",
        stringDelimiters = setOf('"'),
    )

    override val completions = listOf(
        CompletionItem("页面", CompletionKind.KEYWORD, "页面 \"$0\"", "声明网页标题"),
        CompletionItem("文字", CompletionKind.KEYWORD, "文字 \"$0\"", "添加一段文字"),
        CompletionItem("标题", CompletionKind.KEYWORD, "标题 \"$0\"", "添加一个标题"),
        CompletionItem("按钮", CompletionKind.KEYWORD, "按钮 \"$0\"", "添加一个按钮"),
        CompletionItem("卡片", CompletionKind.KEYWORD, "卡片 \"$0\" {\n  $0\n}", "添加卡片容器"),
        CompletionItem("当点击", CompletionKind.SNIPPET, "当点击 \"$1\" 显示 \"$0\"", "点击事件"),
        CompletionItem("颜色", CompletionKind.ATTRIBUTE, "颜色 \"$0\"", "修饰元素颜色"),
        CompletionItem("设置背景", CompletionKind.VALUE, "设置 背景 \"$0\"", "设置页面背景"),
    )

    override val snippets = listOf(
        Snippet("page", "页面 \"我的网页\"\n文字 \"你好，MiniMe！\"\n按钮 \"点我\"\n当点击 \"点我\" 显示 \"你好！\"", "MiniLang 入门骨架", SnippetScope.HTML),
        Snippet("card", "卡片 \"标题\" {\n  文字 \"内容\"\n}", "卡片容器", SnippetScope.HTML),
    )

    override val brackets = listOf(BracketPair('{', '}'), BracketPair('"', '"'))
    override val autoCloseChars = setOf('"', '{')
}
