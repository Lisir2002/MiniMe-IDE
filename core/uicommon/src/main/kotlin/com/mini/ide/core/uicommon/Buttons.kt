package com.mini.ide.core.uicommon

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mini.ide.core.designsystem.Glass
import com.mini.ide.core.designsystem.LocalAppColors

/**
 * 主操作按钮（蓝填充）—— 视觉规范 §7.2。按压 scale 0.97，spring。
 */
@Composable
fun IDEPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = Motion.Spring,
    )
    val bg = if (enabled) colors.primary else colors.secondaryText.copy(alpha = 0.4f)
    Box(
        modifier = modifier
            .height(48.dp)
            .graphicsLayerScale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled) { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Image(
                    painter = rememberVectorPainter(icon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(colors.onPrimary),
                )
                Spacer(Modifier.width(8.dp))
            }
            BasicText(
                text = text,
                style = Typography.bodyStrong.copy(color = colors.onPrimary, textAlign = TextAlign.Center),
            )
        }
    }
}

/**
 * 次按钮（玻璃底 + 描边）—— 视觉规范 §7.2。
 */
@Composable
fun IDESecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = LocalAppColors.current.onBackground,
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, animationSpec = Motion.Spring)
    Box(
        modifier = modifier
            .height(44.dp)
            .graphicsLayerScale(scale)
            .then(Modifier.glass(Glass.Thin, colors.surface, RoundedCornerShape(12.dp)))
            .clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Image(
                    painter = rememberVectorPainter(icon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint),
                )
                Spacer(Modifier.width(6.dp))
            }
            BasicText(text = text, style = Typography.body.copy(color = tint))
        }
    }
}

/** 图标按钮：工具栏用，语义图标 + 玻璃底。 */
@Composable
fun IDEIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalAppColors.current.onBackground,
) {
    val colors = LocalAppColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed && enabled) 0.92f else 1f, animationSpec = Motion.Spring)
    Box(
        modifier = modifier
            .size(40.dp)
            .graphicsLayerScale(scale)
            .clip(RoundedCornerShape(10.dp))
            .then(Modifier.glass(Glass.Thin, colors.surface, RoundedCornerShape(10.dp)))
            .clickable(interactionSource = interaction, indication = null, enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = rememberVectorPainter(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (enabled) tint else colors.secondaryText),
        )
    }
}

/** 统一缩放修饰（graphicsLayer）。 */
private fun Modifier.graphicsLayerScale(scale: Float): Modifier =
    graphicsLayer(scaleX = scale, scaleY = scale)
