package com.mini.ide.data.impl

import com.mini.ide.core.model.Lesson
import com.mini.ide.core.model.LessonStep
import com.mini.ide.core.model.LessonTarget
import com.mini.ide.core.model.ValidatorSpec

/**
 * 内置课程（M5）。完全本地、随 App 打包。
 * 校验为教学向宽松规则：宁可漏报不误报，避免挫败感。
 */
internal object BuiltinLessons {

    private val hello = Lesson(
        id = "lesson-hello",
        title = "第 1 课：我的第一个网页",
        description = "认识 HTML 标题，让文字出现在预览里。",
        steps = listOf(
            LessonStep(
                id = "s1",
                instruction = "在 index.html 的 <body> 里写一个 <h1> 标题标签。",
                hint = "形如 <h1>你好</h1>，记得写结束标签。",
                validator = ValidatorSpec.Contains("<h1"),
            ),
            LessonStep(
                id = "s2",
                instruction = "把标题文字改成「你好」。",
                hint = "<h1>你好</h1>",
                validator = ValidatorSpec.Regex("<h1[^>]*>\\s*你好", ignoreCase = true),
            ),
            LessonStep(
                id = "s3",
                instruction = "点击预览区的「运行」，看看你的网页。",
                hint = "右上角三角按钮就是运行。",
                validator = ValidatorSpec.AstRule("previewRun"),
            ),
        ),
        target = LessonTarget(
            html = "<body><h1>你好</h1></body>",
            description = "目标：页面中央显示一个大标题「你好」。",
        ),
    )

    private val style = Lesson(
        id = "lesson-style",
        title = "第 2 课：给文字上色",
        description = "用 CSS 选择器给标题换上蓝色。",
        steps = listOf(
            LessonStep(
                id = "s1",
                instruction = "在 style.css 里写一个 h1 选择器：h1 { }。",
                hint = "选择器后面跟一对大括号。",
                validator = ValidatorSpec.Contains("h1"),
            ),
            LessonStep(
                id = "s2",
                instruction = "在大括号里加入 color 属性。",
                hint = "形如 color: red;",
                validator = ValidatorSpec.Contains("color"),
            ),
            LessonStep(
                id = "s3",
                instruction = "把颜色改成蓝色 #0A84FF。",
                hint = "color: #0A84FF;",
                validator = ValidatorSpec.Regex("#0a84ff", ignoreCase = true),
            ),
        ),
        target = LessonTarget(
            css = "h1 { color: #0A84FF; }",
            description = "目标：标题文字变成 iOS 蓝。",
        ),
    )

    fun all(): List<Lesson> = listOf(hello, style)
}
