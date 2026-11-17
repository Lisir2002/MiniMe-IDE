package com.mini.ide.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("IDETheme 未提供 AppColors")
}

/**
 * 全局主题入口。暗色为默认主主题，亮色可选（视觉规范已定稿）。
 * M0 仅搭骨架；磨砂玻璃/自绘组件在 M1 填充。
 */
@Composable
fun IDETheme(
    mode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val colors = when (mode) {
        ThemeMode.LIGHT -> LightColors
        ThemeMode.DARK, ThemeMode.SYSTEM -> DarkColors
    }
    CompositionLocalProvider(LocalAppColors provides colors) {
        content()
    }
}
