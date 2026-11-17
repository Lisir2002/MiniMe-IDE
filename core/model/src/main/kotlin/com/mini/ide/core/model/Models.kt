package com.mini.ide.core.model

enum class FileKind { HTML, CSS, JS, DSL }

data class Project(
    val id: String,
    val name: String,
    val templateId: String? = null,
)

data class SourceFile(
    val id: String,
    val projectId: String,
    val name: String,
    val kind: FileKind,
    val content: String = "",
)
