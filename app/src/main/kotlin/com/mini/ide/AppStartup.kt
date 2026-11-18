package com.mini.ide

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.mini.ide.core.log.AndroidLogPaths
import com.mini.ide.core.log.FileLogWriter
import com.mini.ide.core.log.MiniLogger
import com.mini.ide.core.permission.AppPermission
import com.mini.ide.core.permission.PermissionChecker

/**
 * 应用启动编排（app 层）：把「权限状态 → 日志目录 → writer 装配」串起来。
 * 尊重隐私：存储权限未授时先降级到应用专属目录，保证日志不丢；
 * 是否弹 rationale / 跳设置页由 MainActivity 依据 [needsStorageRationale] 决定。
 */
class AppStartup(
    private val context: Context,
    private val logger: MiniLogger,
    private val checker: PermissionChecker,
) {
    /** 外部根目录权限是否已授予（按 API 选择对应权限项）。 */
    fun storageGranted(): Boolean {
        val perm = if (Build.VERSION.SDK_INT >= 30)
            AppPermission.STORAGE_MANAGE else AppPermission.STORAGE_LEGACY
        return checker.isGranted(perm)
    }

    /** 是否需要对用户说明存储权限用途。 */
    fun needsStorageRationale(): Boolean = !storageGranted()

    /**
     * 依据当前权限装配日志 writer 并写启动日志。
     * @return 实际使用的日志目录绝对路径（便于日志中记录降级）
     */
    fun attachLogging(): String {
        val granted = storageGranted()
        val dir = AndroidLogPaths.resolve(context, granted).getOrNull()
        if (dir != null) {
            logger.attach(FileLogWriter(dir))
        }
        val path = dir?.absolutePath ?: "(不可用，仅 logcat)"
        logger.info(
            module = "startup",
            message = "MiniMe-IDE 启动",
            "version" to "1.0.0-rc1",
            "storageGranted" to granted.toString(),
            "logDir" to path,
        )
        return path
    }

    /** 跳系统设置页开启特殊权限（MANAGE_EXTERNAL_STORAGE / 安装未知应用）。 */
    fun settingsIntent(permission: AppPermission): Intent {
        val action = permission.settingsAction
            ?: Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val intent = Intent(action)
        intent.data = Uri.parse("package:${context.packageName}")
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
