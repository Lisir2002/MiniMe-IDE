package com.mini.ide.core.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** 基于 Android framework 的权限检查实现。 */
class AndroidPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) : PermissionChecker {

    override fun isGranted(permission: AppPermission): Boolean = when (permission) {
        AppPermission.STORAGE_MANAGE ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()
        AppPermission.INSTALL_UNKNOWN_APP ->
            context.packageManager.canRequestPackageInstalls()
        else -> ContextCompat.checkSelfPermission(context, permission.manifestName) ==
            PackageManager.PERMISSION_GRANTED
    }
}
