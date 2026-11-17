package com.mini.ide.core.uicommon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mini.ide.core.designsystem.GlassSpec

/**
 * 磨砂玻璃修饰器 —— 对应视觉规范 §2。
 * MVP 采用 REDUCED 级降级：半透明填充 + 1px 描边 + 柔和阴影（不做逐层 RenderEffect 模糊，
 * 性能可控，见视觉规范 §2.4）。模糊层在增强阶段用 RenderEffect 补上，接口不变。
 */
fun Modifier.glass(
    spec: GlassSpec,
    tint: Color,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = Color.White,
    elevation: Dp = 8.dp,
): Modifier = this
    .shadow(elevation = elevation, shape = shape, ambientColor = Color.Black.copy(alpha = 0.4f))
    .background(tint.copy(alpha = spec.fillAlpha), shape)
    .border(width = 1.dp, color = borderColor.copy(alpha = spec.borderAlpha), shape)
