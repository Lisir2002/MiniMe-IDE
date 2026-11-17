package com.mini.ide.data.impl

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun get(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ProjectEntity)

    @Update
    suspend fun update(entity: ProjectEntity)

    @Query("UPDATE projects SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    suspend fun rename(id: String, name: String, updatedAt: Long)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: String)

    @Transaction
    suspend fun insertWithFiles(project: ProjectEntity, files: List<SourceFileEntity>) {
        insert(project)
        files.forEach { insertFile(it) }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(entity: SourceFileEntity)
}

@Dao
interface FileDao {
    @Query("SELECT * FROM source_files WHERE projectId = :projectId ORDER BY sortOrder ASC")
    fun observeByProject(projectId: String): Flow<List<SourceFileEntity>>

    @Query("SELECT * FROM source_files WHERE id = :id")
    suspend fun get(id: String): SourceFileEntity?

    @Query("UPDATE source_files SET content = :content WHERE id = :id")
    suspend fun updateContent(id: String, content: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(files: List<SourceFileEntity>)

    @Query("UPDATE source_files SET content = :content, projectId = :projectId, name = :name, kind = :kind, sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateMeta(id: String, content: String, projectId: String, name: String, kind: String, sortOrder: Int)
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY id ASC")
    suspend fun all(): List<TemplateEntity>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun get(id: String): TemplateEntity?

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(templates: List<TemplateEntity>)
}
