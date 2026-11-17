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
