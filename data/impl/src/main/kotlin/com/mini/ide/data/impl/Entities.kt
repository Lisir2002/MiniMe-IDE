package com.mini.ide.data.impl

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val templateId: String?,
)

@Entity(
    tableName = "source_files",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("projectId")],
)
data class SourceFileEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val kind: String,
    val content: String,
    val sortOrder: Int,
)

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val filesJson: String,
    val isBuiltin: Boolean,
)

/** 课程定义（M5）：steps / target 以 JSON 存，序列化逻辑在 Mappers。 */
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val stepsJson: String,
    val targetHtml: String,
    val targetCss: String,
    val targetJs: String,
    val targetDescription: String,
    val sortOrder: Int,
)

/** 课程进度（M5）：每课程一条，completedStepIds 以逗号分隔存。 */
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val currentStepIndex: Int,
    val completedStepIdsCsv: String,
    val startedAt: Long,
    val updatedAt: Long,
)
