package com.mini.ide.data.impl

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class, SourceFileEntity::class, TemplateEntity::class,
        LessonEntity::class, LessonProgressEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class MiniMeDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun fileDao(): FileDao
    abstract fun templateDao(): TemplateDao
    abstract fun lessonDao(): LessonDao

    companion object {
        const val DATABASE_NAME = "minime.db"

        fun build(context: Context): MiniMeDatabase =
            Room.databaseBuilder(context, MiniMeDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration() // MVP：版本1，尚无迁移；M+ 改为显式 Migration
                .build()

        /** 单测用：内存库，进程内隔离。 */
        fun buildInMemory(context: Context): MiniMeDatabase =
            Room.inMemoryDatabaseBuilder(context, MiniMeDatabase::class.java)
                .allowMainThreadQueries() // 仅测试
                .build()
    }
}
