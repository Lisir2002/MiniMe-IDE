package com.mini.ide.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("IDETheme 未提供 AppColors")
}

/**
 * 全局主题入口。亮色为默认主主题（白底黑字黑线），暗色可选（视觉规范已定稿）。
 */
@Composable
fun IDETheme(
    mode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit,
) {
    val colors = when (mode) {
        ThemeMode.LIGHT, ThemeMode.SYSTEM -> LightColors
        ThemeMode.DARK -> DarkColors
    }
    CompositionLocalProvider(LocalAppColors provides colors) {
        content()
    }
}
