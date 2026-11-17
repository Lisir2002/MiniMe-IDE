package com.mini.ide.data.impl

import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.SourceFileDraft
import com.mini.ide.core.model.Template

/** 内置模板。完全本地、随 App 打包，无云端依赖。 */
internal object BuiltinTemplates {

    private val blankFiles = listOf(
        SourceFileDraft("index.html", FileKind.HTML, BLANK_HTML, 0),
        SourceFileDraft("style.css", FileKind.CSS, "/* 在这里写样式 */\n", 1),
        SourceFileDraft("main.js", FileKind.JS, "// 在这里写 JavaScript\n", 2),
    )

    private val helloFiles = listOf(
        SourceFileDraft(
            "index.html", FileKind.HTML,
            """<!DOCTYPE html>
<html lang="zh">
<head>
  <meta charset="UTF-8">
  <title>我的第一个网页</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <h1 id="title">Hello, MiniMe!</h1>
  <button id="btn">点我</button>
  <script src="main.js"></script>
</body>
</html>
""".trimIndent(), 0,
        ),
        SourceFileDraft(
            "style.css", FileKind.CSS,
            """body {
  font-family: sans-serif;
  text-align: center;
  padding: 40px;
}
#title { color: #0A84FF; }
button {
  padding: 10px 20px;
  border-radius: 12px;
  border: none;
  background: #0A84FF;
  color: white;
}
""".trimIndent(), 1,
        ),
        SourceFileDraft(
            "main.js", FileKind.JS,
            """document.getElementById('btn').addEventListener('click', () => {
  document.getElementById('title').textContent = '你好，MiniMe! 👋';
});
""".trimIndent(), 2,
        ),
    )

    private const val BLANK_HTML = """<!DOCTYPE html>
<html lang="zh">
<head>
  <meta charset="UTF-8">
  <title>我的网页</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <!-- 在这里写 HTML -->
  <script src="main.js"></script>
</body>
</html>
"""

    fun all(): List<Template> = listOf(
        Template("blank", "空白项目", "HTML/CSS/JS 三件套，从零开始", blankFiles, true),
        Template("hello", "Hello World", "带交互的第一个网页示例", helloFiles, true),
    )
}
