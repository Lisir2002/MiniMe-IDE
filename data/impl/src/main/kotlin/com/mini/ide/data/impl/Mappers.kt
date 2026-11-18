package com.mini.ide.data.impl

import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.Lesson
import com.mini.ide.core.model.LessonProgress
import com.mini.ide.core.model.LessonStep
import com.mini.ide.core.model.LessonTarget
import com.mini.ide.core.model.Project
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.SourceFileDraft
import com.mini.ide.core.model.Template
import com.mini.ide.core.model.ValidatorSpec
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

    // ---------- S6 教学（M5）----------

    fun lesson(e: LessonEntity) = Lesson(
        id = e.id,
        title = e.title,
        description = e.description,
        steps = decodeSteps(e.stepsJson),
        target = LessonTarget(e.targetHtml, e.targetCss, e.targetJs, e.targetDescription),
    )

    fun lessonEntity(d: Lesson, sortOrder: Int) = LessonEntity(
        id = d.id,
        title = d.title,
        description = d.description,
        stepsJson = encodeSteps(d.steps),
        targetHtml = d.target.html,
        targetCss = d.target.css,
        targetJs = d.target.js,
        targetDescription = d.target.description,
        sortOrder = sortOrder,
    )

    fun encodeSteps(steps: List<LessonStep>): String {
        val arr = JSONArray()
        steps.forEach { s ->
            val v = JSONObject().apply {
                when (val spec = s.validator) {
                    is ValidatorSpec.Contains -> {
                        put("type", "Contains"); put("text", spec.text)
                    }
                    is ValidatorSpec.Regex -> {
                        put("type", "Regex"); put("pattern", spec.pattern); put("ignoreCase", spec.ignoreCase)
                    }
                    is ValidatorSpec.AstRule -> {
                        put("type", "AstRule"); put("rule", spec.rule)
                    }
                }
            }
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("instruction", s.instruction)
                put("hint", s.hint ?: JSONObject.NULL)
                put("validator", v)
            })
        }
        return arr.toString()
    }

    fun decodeSteps(json: String): List<LessonStep> {
        if (json.isBlank()) return emptyList()
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val v = o.getJSONObject("validator")
            val spec: ValidatorSpec = when (v.optString("type")) {
                "Regex" -> ValidatorSpec.Regex(
                    v.optString("pattern"),
                    v.optBoolean("ignoreCase", true),
                )
                "AstRule" -> ValidatorSpec.AstRule(v.optString("rule"))
                else -> ValidatorSpec.Contains(v.optString("text"))
            }
            val hintRaw = o.opt("hint")
            LessonStep(
                id = o.getString("id"),
                instruction = o.getString("instruction"),
                hint = if (hintRaw == null || hintRaw === JSONObject.NULL) null else hintRaw.toString(),
                validator = spec,
            )
        }
    }

    fun progress(e: LessonProgressEntity) = LessonProgress(
        lessonId = e.lessonId,
        currentStepIndex = e.currentStepIndex,
        completedStepIds = e.completedStepIdsCsv.split(",").filter { it.isNotBlank() }.toSet(),
        startedAt = e.startedAt,
        updatedAt = e.updatedAt,
    )

    fun progressEntity(d: LessonProgress) = LessonProgressEntity(
        lessonId = d.lessonId,
        currentStepIndex = d.currentStepIndex,
        completedStepIdsCsv = d.completedStepIds.joinToString(","),
        startedAt = d.startedAt,
        updatedAt = d.updatedAt,
    )
}
