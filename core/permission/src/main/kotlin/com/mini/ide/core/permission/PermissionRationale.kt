package com.mini.ide.core.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mini.ide.core.designsystem.Glass
import com.mini.ide.core.designsystem.LocalAppColors
import com.mini.ide.core.uicommon.IDESecondaryButton
import com.mini.ide.core.uicommon.Typography
import com.mini.ide.core.uicommon.glass

/**
 * 权限用途说明对话框（隐私合规：用前告知，用户知情同意）。
 * 玻璃卡片风格，复用自绘组件，视觉与全局一致。
 */
@Composable
fun PermissionRationaleDialog(
    permission: AppPermission,
    onAllow: () -> Unit,
    onDeny: () -> Unit,
) {
    val colors = LocalAppColors.current
    Dialog(onDismissRequest = onDeny) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .glass(Glass.Thick, colors.surface, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText(
                text = "需要「${domainName(permission)}」权限",
                style = Typography.bodyStrong.copy(color = colors.onBackground, textAlign = TextAlign.Center),
            )
            Spacer(Modifier.height(12.dp))
            BasicText(
                text = permission.purpose,
                style = Typography.body.copy(color = colors.secondaryText, textAlign = TextAlign.Center),
            )
            Spacer(Modifier.height(8.dp))
            BasicText(
                text = "你可以拒绝；应用会改用安全降级方案，不影响核心功能。",
                style = Typography.caption.copy(color = colors.secondaryText, textAlign = TextAlign.Center),
            )
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    IDESecondaryButton(text = "暂不", onClick = onDeny, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    com.mini.ide.core.uicommon.IDEPrimaryButton(
                        text = "允许", onClick = onAllow, modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun domainName(p: AppPermission): String = when (p) {
    AppPermission.STORAGE_LEGACY, AppPermission.STORAGE_MANAGE -> "存储"
    AppPermission.NETWORK, AppPermission.NETWORK_STATE -> "网络"
    AppPermission.LOCATION -> "定位"
    AppPermission.CAMERA -> "相机"
    AppPermission.OPEN_EXTERNAL_APP -> "打开外部应用"
    AppPermission.INSTALL_UNKNOWN_APP -> "安装未知应用"
}
