package com.mini.ide.core.uicommon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mini.ide.core.designsystem.Glass
import com.mini.ide.core.designsystem.LocalAppColors

/**
 * 顶部吸顶玻璃栏 —— 视觉规范 §7.2 IDETopBar（Thick）+ §6 语义图标。
 * 左：导航动作（如返回）；中：标题；右：动作槽。
 */
@Composable
fun IDETopBar(
    title: String,
    modifier: Modifier = Modifier,
    navIcon: ImageVector? = null,
    onNavClick: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = LocalAppColors.current
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(Modifier.glass(Glass.Thick, colors.surface, androidx.compose.foundation.shape.RoundedCornerShape(0.dp)))
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (navIcon != null && onNavClick != null) {
                IDEIconButton(icon = navIcon, onClick = onNavClick, contentDescription = "返回")
                Spacer(Modifier.width(8.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                BasicText(text = title, style = Typography.bodyStrong.copy(color = colors.onBackground))
                if (subtitle != null) {
                    BasicText(
                        text = subtitle,
                        style = Typography.caption.copy(color = colors.secondaryText),
                    )
                }
            }
            Row(content = actions)
        }
        // 细底边
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(colors.separator)
                .align(Alignment.BottomCenter)
        )
    }
}
