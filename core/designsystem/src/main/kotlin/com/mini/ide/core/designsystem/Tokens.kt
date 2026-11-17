package com.mini.ide.core.designsystem

import androidx.compose.ui.graphics.Color

/** 主题模式 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** 语义化颜色令牌（数值取自视觉规范 §1） */
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
    separator = Color(0x802A2A2F),
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
    separator = Color(0x99E2E2E8),
    error = Color(0xFFD70015),
    success = Color(0xFF248A3D),
    warning = Color(0xFFB88000),
)

/** 玻璃层级（视觉规范 §2） */
data class GlassSpec(val blurDp: Float, val fillAlpha: Float, val borderAlpha: Float)

object Glass {
    val Thin = GlassSpec(12f, 0.08f, 0.10f)
    val Regular = GlassSpec(20f, 0.12f, 0.15f)
    val Thick = GlassSpec(28f, 0.18f, 0.20f)
}
