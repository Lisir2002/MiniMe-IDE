package com.mini.ide.core.uicommon

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mini.ide.core.designsystem.LocalAppColors
import com.mini.ide.core.designsystem.Surface

/**
 * 卡片：实色填充 + 细描边 + 轻阴影，20dp 圆角。按压 scale 0.98 反馈。
 */
@Composable
fun IDECard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.98f else 1f,
        animationSpec = Motion.Spring,
    )
    Column(
        modifier = modifier
            .graphicsLayerCompat(scale)
            .then(
                Modifier.surface(
                    spec = Surface.Regular,
                    color = colors.surface,
                    shape = RoundedCornerShape(20.dp),
                    borderColor = colors.separator,
                )
            )
            .then(if (onClick != null) Modifier.clickable(interactionSource = interaction, indication = null) { onClick() } else Modifier)
            .padding(16.dp),
        content = content,
    )
}

private fun Modifier.graphicsLayerCompat(scale: Float): Modifier =
    graphicsLayer(scaleX = scale, scaleY = scale)
