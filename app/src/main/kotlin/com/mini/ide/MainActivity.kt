package com.mini.ide

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mini.ide.core.designsystem.IDETheme
import com.mini.ide.core.designsystem.ThemeMode as UiThemeMode
import com.mini.ide.core.log.MiniLogger
import com.mini.ide.core.model.ThemeMode as DataThemeMode
import com.mini.ide.core.permission.AppPermission
import com.mini.ide.core.permission.PermissionChecker
import com.mini.ide.core.permission.PermissionManager
import com.mini.ide.core.permission.PermissionRationaleDialog
import com.mini.ide.data.api.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepo: SettingsRepository

    @Inject
    lateinit var logger: MiniLogger

    @Inject
    lateinit var checker: PermissionChecker

    @Inject
    lateinit var permissionManager: PermissionManager

    private val _themeMode = MutableStateFlow(UiThemeMode.LIGHT)
    private val themeMode: StateFlow<UiThemeMode> = _themeMode

    private val _showStorageRationale = MutableStateFlow(false)
    private val showStorageRationale: StateFlow<Boolean> = _showStorageRationale

    private lateinit var appStartup: AppStartup
    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null
    private var settingsLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 沉浸式：内容延伸到状态栏下，由 MiniMeNavHost 统一消费 statusBars insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        appStartup = AppStartup(this, logger, checker)

        // dangerous 权限（API≤28 存储写）运行时申请结果
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                logger.info("permission", "存储权限申请结果", "granted" to granted.toString())
                appStartup.attachLogging() // 授权则用外部根目录，拒绝则降级，两种都重装配
                _showStorageRationale.value = false
            }
        // special 权限（MANAGE_EXTERNAL_STORAGE）跳设置页返回
        settingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val granted = appStartup.storageGranted()
                logger.info("permission", "从系统设置页返回", "storageGranted" to granted.toString())
                appStartup.attachLogging()
                if (granted) _showStorageRationale.value = false
            }

        // 首次装配日志（未授权时自动降级到应用专属目录，日志不丢）
        appStartup.attachLogging()
        // 存储权限未授 → 用前告知并申请（隐私合规）
        if (appStartup.needsStorageRationale()) {
            _showStorageRationale.value = true
        }

        lifecycleScope.launch {
            settingsRepo.observe().collect { settings ->
                _themeMode.value = when (settings.themeMode) {
                    DataThemeMode.LIGHT -> UiThemeMode.LIGHT
                    DataThemeMode.DARK -> UiThemeMode.DARK
                    DataThemeMode.SYSTEM -> UiThemeMode.SYSTEM
                }
            }
        }

        setContent {
            val mode by themeMode.collectAsStateWithLifecycle()
            val showRationale by showStorageRationale.collectAsStateWithLifecycle()
            IDETheme(mode = mode) {
                MiniMeNavHost()
                if (showRationale) {
                    val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        AppPermission.STORAGE_MANAGE else AppPermission.STORAGE_LEGACY
                    PermissionRationaleDialog(
                        permission = perm,
                        onAllow = { requestStoragePermission(perm) },
                        onDeny = {
                            _showStorageRationale.value = false
                            logger.info("permission", "用户暂拒存储权限，日志降级到应用专属目录")
                        },
                    )
                }
            }
        }
    }

    /** 依据权限类型走运行时弹窗或系统设置页。 */
    private fun requestStoragePermission(permission: AppPermission) {
        permissionManager.markRequested(permission)
        when (permission.kind) {
            com.mini.ide.core.permission.PermissionKind.SPECIAL ->
                settingsLauncher?.launch(appStartup.settingsIntent(permission))
            com.mini.ide.core.permission.PermissionKind.DANGEROUS ->
                requestPermissionLauncher?.launch(permission.manifestName)
            com.mini.ide.core.permission.PermissionKind.NORMAL -> Unit
        }
    }
}
