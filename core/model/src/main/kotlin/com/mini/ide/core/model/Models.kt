package com.mini.ide.core.model

/** 源码文件类型。DSL 为 Phase3 自研语言预留。 */
enum class FileKind { HTML, CSS, JS, DSL }

/** 主题模式。跟随系统 / 亮色 / 暗色。 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** 一个项目（教学作品）。时间戳用于列表排序与"最近打开"。 */
data class Project(
    val id: String,
    val name: String,
    val templateId: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

/** 项目内的一个源码文件。content 存于 Room（MVP 结论）。 */
data class SourceFile(
    val id: String,
    val projectId: String,
    val name: String,
    val kind: FileKind,
    val content: String = "",
    val sortOrder: Int = 0,
)

/** 模板中一个文件的草稿（用于模板实例化，无 id / projectId）。 */
data class SourceFileDraft(
    val name: String,
    val kind: FileKind,
    val content: String,
    val sortOrder: Int = 0,
)

/** 内置或自定义模板。 */
data class Template(
    val id: String,
    val name: String,
    val description: String,
    val files: List<SourceFileDraft>,
    val isBuiltin: Boolean,
)

/** 全局设置。 */
data class Settings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val editorFontSizeSp: Int = 14,
    val wordWrap: Boolean = false,
    val previewDebounceMs: Long = 300L,
    val autoSave: Boolean = true,
    val sandboxMode: Boolean = true,
    /** Phase2 教学功能开关（教程入口的"已启用引导"状态）。 */
    val lessonEnabled: Boolean = false,
)
