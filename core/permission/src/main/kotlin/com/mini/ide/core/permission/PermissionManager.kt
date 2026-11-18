package com.mini.ide.core.permission

/**
 * 权限检查抽象：隔离 Android framework，便于纯 JVM 单测。
 * Android 实现用 ContextCompat.checkSelfPermission；
 * special 权限用 AppOpsManager / Settings.canDrawOverlays 等。
 */
interface PermissionChecker {
    fun isGranted(permission: AppPermission): Boolean
}

/** 权限申请结果。 */
enum class GrantResult { GRANTED, DENIED, PERMANENTLY_DENIED }

/**
 * 权限决策核心（纯 JVM 可测）：根据已授权状态与是否曾请求，给出流程建议。
 * - NORMAL：视为已授，无需流程。
 * - DANGEROUS：未授 → 若首次则弹 rationale，否则直接申请；曾拒且不再询问 → 引导设置页。
 * - SPECIAL：未授 → 引导设置页。
 */
class PermissionManager(private val checker: PermissionChecker) {

    /** 记录已请求过的权限（决定是否需要先展示 rationale）。 */
    private val requested = mutableSetOf<AppPermission>()

    fun status(permission: AppPermission): GrantResult = when {
        permission.kind == PermissionKind.NORMAL -> GrantResult.GRANTED
        checker.isGranted(permission) -> GrantResult.GRANTED
        else -> GrantResult.DENIED
    }

    /** 是否需要先向用户展示用途说明（rationale）。 */
    fun needsRationale(permission: AppPermission): Boolean =
        permission.kind == PermissionKind.DANGEROUS &&
            permission.proactive &&
            !checker.isGranted(permission) &&
            permission !in requested

    /** 标记已发起申请（rationale 展示后 / 直接申请前调用）。 */
    fun markRequested(permission: AppPermission) {
        requested += permission
    }

    /** 应走的系统流程。 */
    fun flow(permission: AppPermission): PermissionFlow = when {
        status(permission) == GrantResult.GRANTED -> PermissionFlow.NONE
        permission.kind == PermissionKind.SPECIAL -> PermissionFlow.SETTINGS
        permission.kind == PermissionKind.DANGEROUS -> PermissionFlow.RUNTIME_DIALOG
        else -> PermissionFlow.NONE
    }

    fun reset() = requested.clear()
}

enum class PermissionFlow { NONE, RUNTIME_DIALOG, SETTINGS }
