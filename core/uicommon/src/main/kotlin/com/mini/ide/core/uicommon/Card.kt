package com.mini.ide.core.uicommon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mini.ide.core.designsystem.Glass
import com.mini.ide.core.designsystem.LocalAppColors

/**
 * 卡片 —— 视觉规范 §7.2 IDECard（Regular，20dp 圆角、细描边、柔和阴影）。
 * 按压 scale 0.98，轻盈反馈。
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
            .then(Modifier.glass(Glass.Regular, colors.surface, RoundedCornerShape(20.dp)))
            .then(if (onClick != null) Modifier.clickable(interactionSource = interaction, indication = null) { onClick() } else Modifier)
            .padding(16.dp),
        content = content,
    )
}

private fun Modifier.graphicsLayerCompat(scale: Float): Modifier =
    graphicsLayer(scaleX = scale, scaleY = scale)
