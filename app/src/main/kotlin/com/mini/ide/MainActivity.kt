package com.mini.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.mini.ide.core.designsystem.IDETheme
import com.mini.ide.core.designsystem.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 沉浸式：内容延伸到状态栏下，配合磨砂玻璃顶栏
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            IDETheme(mode = ThemeMode.DARK) {
                MiniMeNavHost()
            }
        }
    }
}
