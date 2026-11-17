package com.mini.ide.data.impl

/** 源码内容存储抽象。MVP 用 Room 实现；未来可替换为文件系统，Repository 与上层无感。 */
interface ContentStore {
    suspend fun read(fileId: String): String
    suspend fun write(fileId: String, content: String)
}

/** MVP 实现：内容存 Room（与 SourceFileEntity.content 同一张表）。 */
class DbContentStore(private val dao: FileDao) : ContentStore {
    override suspend fun read(fileId: String): String = dao.get(fileId)?.content ?: ""

    override suspend fun write(fileId: String, content: String) = dao.updateContent(fileId, content)
}
