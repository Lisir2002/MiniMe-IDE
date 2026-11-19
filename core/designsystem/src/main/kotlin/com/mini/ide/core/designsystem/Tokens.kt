package com.mini.ide.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 主题模式 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** 语义化颜色令牌（实色，不使用半透明玻璃） */
data class AppColors(
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val secondaryText: Color,
    val primary: Color,
    val onPrimary: Color,
    val separator: Color,
    val error: Color,
    val success: Color,
    val warning: Color,
)

val DarkColors = AppColors(
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1F),
    onBackground = Color(0xFFECECF2),
    secondaryText = Color(0xFF6E6E7A),
    primary = Color(0xFF0A84FF),
    onPrimary = Color(0xFFFFFFFF),
    separator = Color(0xFF2A2A2F),
    error = Color(0xFFFF453A),
    success = Color(0xFF30D158),
    warning = Color(0xFFFFD60A),
)

val LightColors = AppColors(
    background = Color(0xFFF7F7F9),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1C22),
    secondaryText = Color(0xFF8A8A96),
    primary = Color(0xFF007AFF),
    onPrimary = Color(0xFFFFFFFF),
    separator = Color(0xFFE2E2E8),
    error = Color(0xFFD70015),
    success = Color(0xFF248A3D),
    warning = Color(0xFFB88000),
)

/**
 * 表面层级：普通实色表面样式。
 * 仅用「描边 + 阴影」区分层级，不使用模糊 / 半透明填充。
 */
data class SurfaceSpec(val borderWidth: Dp, val elevation: Dp)

object Surface {
    /** 轻：细描边、无阴影（图标按钮、次按钮） */
    val Thin = SurfaceSpec(borderWidth = 1.dp, elevation = 0.dp)

    /** 常规：细描边 + 轻阴影（卡片、弹窗） */
    val Regular = SurfaceSpec(borderWidth = 1.dp, elevation = 2.dp)

    /** 厚：无描边、无阴影（吸顶栏、底栏，靠分隔线区分） */
    val Thick = SurfaceSpec(borderWidth = 0.dp, elevation = 0.dp)
}
