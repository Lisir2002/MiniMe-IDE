package com.mini.ide.core.log

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * 日志目录解析：优先外部存储根目录下的 MINIIDE/logs（用户明确要求），
 * 无权限/不可写时降级到应用专属外部目录，保证日志永不丢失。
 */
object AndroidLogPaths {

    const val ROOT_FOLDER = "MINIIDE"
    const val LOGS_FOLDER = "logs"

    /**
     * 解析日志根目录（含 logs 子目录），返回可写目录。
     * @param externalRootGranted 是否已获 MANAGE_EXTERNAL_STORAGE（"所有文件访问"）
     */
    fun resolve(context: Context, externalRootGranted: Boolean): Result<File> {
        if (externalRootGranted) {
            runCatching {
                val root = File(Environment.getExternalStorageDirectory(), ROOT_FOLDER)
                val logs = File(root, LOGS_FOLDER)
                if (ensureWritable(logs)) return Result.success(logs)
            }
        }
        // 降级：应用专属外部目录，无需任何权限
        val fallback = File(context.getExternalFilesDir(null), "$ROOT_FOLDER/$LOGS_FOLDER")
        return if (ensureWritable(fallback)) {
            Result.success(fallback)
        } else {
            Result.failure(IllegalStateException("日志目录均不可写"))
        }
    }

    private fun ensureWritable(dir: File): Boolean {
        val created = if (dir.exists()) true else dir.mkdirs()
        if (!created) return false
        val probe = File(dir, ".write_probe")
        return runCatching {
            probe.writeText("ok")
            probe.delete()
        }.getOrDefault(false)
    }
}
