package com.mini.ide.core.permission

import android.Manifest

/**
 * 权限申请方式分类（决定 UI 与流程）：
 * - NORMAL：安装即授，无需运行时申请（INTERNET / ACCESS_NETWORK_STATE / QUERY_ALL_PACKAGES）
 * - DANGEROUS：运行时弹系统对话框（存储读写 ≤API28、定位、相机）
 * - SPECIAL：需跳转系统设置页手动开启（MANAGE_EXTERNAL_STORAGE、REQUEST_INSTALL_PACKAGES）
 */
enum class PermissionKind { NORMAL, DANGEROUS, SPECIAL }

/**
 * 应用涉及的 6 类权限（隐私合规唯一清单）。
 * @param manifestName Manifest 中声明的权限名（special/dangerous 有效；NORMAL 仅声明）
 * @param kind 申请方式
 * @param purpose 用途说明（rationale 展示给用户，尊重知情权）
 * @param minSdk / maxSdk 适用范围（minSdk inclusive, maxSdk inclusive，null 表示不限）
 * @param proactive 是否在用前主动申请（false=仅预留入口，当前无对应功能，不主动打扰用户）
 */
enum class AppPermission(
    val manifestName: String,
    val kind: PermissionKind,
    val purpose: String,
    val minSdk: Int = 1,
    val maxSdk: Int? = null,
    val proactive: Boolean = true,
    val settingsAction: String? = null,
) {
    /** 网络：normal 级，安装即授；预览为本地 WebView 渲染，不上传任何用户数据。 */
    NETWORK(
        manifestName = Manifest.permission.INTERNET,
        kind = PermissionKind.NORMAL,
        purpose = "用于本地预览服务的环回通信，不上传你的代码或任何数据。",
    ),
    NETWORK_STATE(
        manifestName = Manifest.permission.ACCESS_NETWORK_STATE,
        kind = PermissionKind.NORMAL,
        purpose = "读取网络状态以判断在线能力，不改变你的网络配置。",
    ),
    /** 存储：日志写入外部根目录 MINIIDE 所需。API≤28 用 WRITE_EXTERNAL_STORAGE，API≥30 用 MANAGE_EXTERNAL_STORAGE。 */
    STORAGE_LEGACY(
        manifestName = Manifest.permission.WRITE_EXTERNAL_STORAGE,
        kind = PermissionKind.DANGEROUS,
        purpose = "允许把运行日志写入外部存储的 MINIIDE 目录，便于故障时排查。",
        maxSdk = 28,
    ),
    STORAGE_MANAGE(
        manifestName = Manifest.permission.MANAGE_EXTERNAL_STORAGE,
        kind = PermissionKind.SPECIAL,
        purpose = "允许把运行日志写入外部存储根目录的 MINIIDE 文件夹；拒绝则日志改存应用专属目录。",
        minSdk = 30,
        settingsAction = "android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION",
    ),
    /** 定位：当前无定位功能，仅预留声明与申请入口，不主动申请。 */
    LOCATION(
        manifestName = Manifest.permission.ACCESS_FINE_LOCATION,
        kind = PermissionKind.DANGEROUS,
        purpose = "预留：未来基于位置的示例教学会用到；当前版本不收集你的位置。",
        proactive = false,
    ),
    /** 相机：当前无相机功能，仅预留。 */
    CAMERA(
        manifestName = Manifest.permission.CAMERA,
        kind = PermissionKind.DANGEROUS,
        purpose = "预留：未来扫码/拍照示例会用到；当前版本不调用相机。",
        proactive = false,
    ),
    /** 打开外部应用：Android 11+ 包可见性，normal 级声明。 */
    OPEN_EXTERNAL_APP(
        manifestName = "android.permission.QUERY_ALL_PACKAGES",
        kind = PermissionKind.NORMAL,
        purpose = "用于在教程中打开外部浏览器等应用，不读取你的应用列表数据。",
    ),
    /** 安装未知应用：special 级，跳设置页；预留 OTA 更新入口，不主动申请。 */
    INSTALL_UNKNOWN_APP(
        manifestName = Manifest.permission.REQUEST_INSTALL_PACKAGES,
        kind = PermissionKind.SPECIAL,
        purpose = "预留：未来应用内更新安装 APK 会用到；当前版本不安装任何应用。",
        proactive = false,
        settingsAction = "android.settings.MANAGE_UNKNOWN_APP_SOURCES",
    );

    /** 该权限是否适用于当前 API 版本。 */
    fun appliesTo(api: Int): Boolean = api >= minSdk && (maxSdk == null || api <= maxSdk)

    companion object {
        /** 6 大功能域（对外展示口径），映射到具体权限项。 */
        val DOMAINS = listOf("存储", "网络", "定位", "相机", "打开外部应用", "安装未知应用")

        /** 当前 API 下所有适用权限。 */
        fun applicable(api: Int): List<AppPermission> = entries.filter { it.appliesTo(api) }
    }
}
