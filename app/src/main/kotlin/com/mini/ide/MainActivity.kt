package com.mini.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mini.ide.core.designsystem.IDETheme
import com.mini.ide.core.designsystem.ThemeMode as UiThemeMode
import com.mini.ide.core.model.ThemeMode as DataThemeMode
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

    private val _themeMode = MutableStateFlow(UiThemeMode.DARK)
    private val themeMode: StateFlow<UiThemeMode> = _themeMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 沉浸式：内容延伸到状态栏下，配合磨砂玻璃顶栏
        WindowCompat.setDecorFitsSystemWindows(window, false)

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
            IDETheme(mode = mode) {
                MiniMeNavHost()
            }
        }
    }
}
