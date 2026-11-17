package com.mini.ide.language.impl

import com.mini.ide.language.api.BracketPair
import com.mini.ide.language.api.CompletionItem
import com.mini.ide.language.api.CompletionKind
import com.mini.ide.language.api.HighlightSpec
import com.mini.ide.language.api.LanguageId
import com.mini.ide.language.api.LanguageProfile
import com.mini.ide.language.api.Snippet
import com.mini.ide.language.api.SnippetScope

/**
 * 教学向内置语言配置：HTML / CSS / JS 三套 Profile。
 *
 * 数据驱动、非工业全量——只覆盖新手最常用的标签、属性、属性/关键字与代码片段。
 * Phase3 自研 DSL 新增一个 Profile 注册即可，编辑器内核零改动。
 */

object HtmlProfile : LanguageProfile {
    override val id = LanguageId.HTML
    override val displayName = "HTML"
    override val fileExtensions = listOf("html", "htm")
    override val codemirrorMode = "text/html"
    override val highlight = HighlightSpec(
        keywords = setOf("html", "head", "body", "div", "span", "p", "a", "img", "ul", "li", "h1", "h2", "button"),
        stringDelimiters = setOf('"', '\''),
        commentPrefix = "<!--",
    )
    override val completions = listOf(
        CompletionItem("div", CompletionKind.TAG, "<div>\n  \$0\n</div>", "块级容器"),
        CompletionItem("p", CompletionKind.TAG, "<p>\$0</p>", "段落"),
        CompletionItem("a", CompletionKind.TAG, "<a href=\"\$0\">link</a>", "链接"),
        CompletionItem("img", CompletionKind.TAG, "<img src=\"\$0\" alt=\"\">", "图片"),
        CompletionItem("class", CompletionKind.ATTRIBUTE, "class=\"\$0\"", "CSS 类"),
        CompletionItem("id", CompletionKind.ATTRIBUTE, "id=\"\$0\"", "唯一标识"),
    )
    override val snippets = listOf(
        Snippet("html", "<!DOCTYPE html>\n<html>\n<head><meta charset=\"utf-8\"><title>\$1</title></head>\n<body>\n\$0\n</body>\n</html>", "HTML 骨架", SnippetScope.HTML),
    )
    override val brackets = listOf(BracketPair('<', '>'), BracketPair('(', ')'))
    override val autoCloseChars = setOf('"', '\'')
}

object CssProfile : LanguageProfile {
    override val id = LanguageId.CSS
    override val displayName = "CSS"
    override val fileExtensions = listOf("css")
    override val codemirrorMode = "text/css"
    override val highlight = HighlightSpec(
        keywords = setOf("color", "background", "margin", "padding", "font-size", "display", "width", "height"),
        stringDelimiters = setOf('"', '\''),
        commentPrefix = "/*",
    )
    override val completions = listOf(
        CompletionItem("color", CompletionKind.PROPERTY, "color: \$0;", "文字颜色"),
        CompletionItem("background", CompletionKind.PROPERTY, "background: \$0;", "背景"),
        CompletionItem("margin", CompletionKind.PROPERTY, "margin: \$0;", "外边距"),
        CompletionItem("padding", CompletionKind.PROPERTY, "padding: \$0;", "内边距"),
        CompletionItem("flex", CompletionKind.VALUE, "flex", "弹性布局"),
    )
    override val snippets = listOf(
        Snippet("bg", "background-color: \$0;", "背景色", SnippetScope.CSS),
    )
    override val brackets = listOf(BracketPair('{', '}'), BracketPair('(', ')'))
    override val autoCloseChars = setOf('"', '\'')
}

object JsProfile : LanguageProfile {
    override val id = LanguageId.JS
    override val displayName = "JavaScript"
    override val fileExtensions = listOf("js", "mjs")
    override val codemirrorMode = "text/javascript"
    override val highlight = HighlightSpec(
        keywords = setOf("let", "const", "var", "function", "return", "if", "else", "for", "while", "class", "new"),
        builtins = setOf("console", "document", "window", "Math"),
        stringDelimiters = setOf('"', '\'', '`'),
        commentPrefix = "//",
    )
    override val completions = listOf(
        CompletionItem("console.log", CompletionKind.KEYWORD, "console.log(\$0)", "打印日志"),
        CompletionItem("let", CompletionKind.KEYWORD, "let \$0 = ", "声明变量"),
        CompletionItem("function", CompletionKind.KEYWORD, "function \$0() {\n  \$1\n}", "函数"),
        CompletionItem("querySelector", CompletionKind.PROPERTY, "document.querySelector(\"\$0\")", "选择元素"),
    )
    override val snippets = listOf(
        Snippet("log", "console.log(\$0);", "打印", SnippetScope.JS),
        Snippet("fn", "function \$1() {\n  \$0\n}", "函数", SnippetScope.JS),
    )
    override val brackets = listOf(BracketPair('{', '}'), BracketPair('(', ')'), BracketPair('[', ']'))
    override val autoCloseChars = setOf('"', '\'', '`')
}
