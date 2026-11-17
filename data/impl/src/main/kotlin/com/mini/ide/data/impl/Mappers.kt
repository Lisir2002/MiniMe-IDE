package com.mini.ide.data.impl

import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.Project
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.SourceFileDraft
import com.mini.ide.core.model.Template
import org.json.JSONArray
import org.json.JSONObject

/** Entity ↔ 领域模型映射。contains 序列化逻辑，供 Repository 与内置模板 seed 复用。 */
internal object Mappers {

    fun project(e: ProjectEntity) = Project(
        id = e.id,
        name = e.name,
        templateId = e.templateId,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt,
    )

    fun projectEntity(d: Project) = ProjectEntity(
        id = d.id,
        name = d.name,
        templateId = d.templateId,
        createdAt = d.createdAt,
        updatedAt = d.updatedAt,
    )

    fun file(e: SourceFileEntity) = SourceFile(
        id = e.id,
        projectId = e.projectId,
        name = e.name,
        kind = runCatching { FileKind.valueOf(e.kind) }.getOrDefault(FileKind.HTML),
        content = e.content,
        sortOrder = e.sortOrder,
    )

    fun fileEntity(d: SourceFile) = SourceFileEntity(
        id = d.id,
        projectId = d.projectId,
        name = d.name,
        kind = d.kind.name,
        content = d.content,
        sortOrder = d.sortOrder,
    )

    fun template(e: TemplateEntity) = Template(
        id = e.id,
        name = e.name,
        description = e.description,
        files = decodeFiles(e.filesJson),
        isBuiltin = e.isBuiltin,
    )

    fun templateEntity(d: Template) = TemplateEntity(
        id = d.id,
        name = d.name,
        description = d.description,
        filesJson = encodeFiles(d.files),
        isBuiltin = d.isBuiltin,
    )

    fun encodeFiles(files: List<SourceFileDraft>): String {
        val arr = JSONArray()
        files.forEach { f ->
            arr.put(JSONObject().apply {
                put("name", f.name)
                put("kind", f.kind.name)
                put("content", f.content)
                put("sortOrder", f.sortOrder)
            })
        }
        return arr.toString()
    }

    fun decodeFiles(json: String): List<SourceFileDraft> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            SourceFileDraft(
                name = o.getString("name"),
                kind = runCatching { FileKind.valueOf(o.getString("kind")) }.getOrDefault(FileKind.HTML),
                content = o.optString("content"),
                sortOrder = o.optInt("sortOrder"),
            )
        }
    }
}
