package com.mini.ide.core.uicommon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.mini.ide.core.designsystem.SurfaceSpec

/**
 * 普通实色表面修饰器。
 *
 * 实色填充 + 清晰描边 + 轻量阴影；不使用半透明填充、RenderEffect 模糊或磨砂玻璃材质。
 * 层级差异仅由 [SurfaceSpec] 的描边宽度与阴影高度表达。
 */
fun Modifier.surface(
    spec: SurfaceSpec,
    color: Color,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = Color.Unspecified,
): Modifier = this
    .shadow(
        elevation = spec.elevation,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = 0.05f),
        spotColor = Color.Black.copy(alpha = 0.10f),
    )
    .background(color, shape)
    .then(
        if (spec.borderWidth > 0.dp && borderColor != Color.Unspecified) {
            border(width = spec.borderWidth, color = borderColor, shape = shape)
        } else {
            Modifier
        }
    )
