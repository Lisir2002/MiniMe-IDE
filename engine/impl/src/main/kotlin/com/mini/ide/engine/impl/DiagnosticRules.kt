package com.mini.ide.engine.impl

import com.mini.ide.engine.api.DiagnosticCategory
import com.mini.ide.engine.api.LogLevel

/**
 * 教学诊断规则表（数据驱动，M5）。
 * 命中关键词 → 分类 + 一句话标题 + 中文教学建议。宽松匹配，宁漏勿误。
 * 顺序敏感：先匹配更具体的规则。
 */
internal object DiagnosticRules {

    data class Rule(
        val keywords: List<String>,
        val category: DiagnosticCategory,
        val title: String,
        val suggestion: String,
        val severity: LogLevel = LogLevel.ERROR,
        val ignoreCase: Boolean = true,
    )

    val rules: List<Rule> = listOf(
        Rule(
            keywords = listOf("is not defined", "referenceerror"),
            category = DiagnosticCategory.RUNTIME,
            title = "用了一个还没定义的名字",
            suggestion = "检查拼写，或在使用前先声明这个变量/函数。例如先写 var 名字 = 值;",
        ),
        Rule(
            keywords = listOf("cannot read propert", "cannot set propert", "null or undefined"),
            category = DiagnosticCategory.RUNTIME,
            title = "访问了不存在的对象属性",
            suggestion = "对象可能还没创建成功。先确认 id 是否匹配、元素是否加载，再访问它的属性。",
        ),
        Rule(
            keywords = listOf("is not a function", "not a function"),
            category = DiagnosticCategory.RUNTIME,
            title = "把一个不是函数的东西当函数调用了",
            suggestion = "检查名字是否与变量重名，或该 API 是否拼写正确。",
        ),
        Rule(
            keywords = listOf("syntaxerror", "unexpected token", "missing )", "unexpected end"),
            category = DiagnosticCategory.SYNTAX,
            title = "代码写法有误（语法错误）",
            suggestion = "检查括号、引号是否成对闭合，语句结尾是否需要分号。",
        ),
        Rule(
            keywords = listOf("404", "failed to load resource", "net::err"),
            category = DiagnosticCategory.NETWORK,
            title = "引用的文件没找到",
            suggestion = "检查 HTML 里 link/script 的 href/src 文件名，是否与实际文件一致（如 style.css、main.js）。",
        ),
        Rule(
            keywords = listOf("typeerror"),
            category = DiagnosticCategory.RUNTIME,
            title = "数据类型用错了",
            suggestion = "确认变量是字符串、数字还是对象，用错了类型的方法会报这个错。",
        ),
    )
}
