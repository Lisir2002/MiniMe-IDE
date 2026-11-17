package com.mini.ide.feature.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mini.ide.core.designsystem.LocalAppColors
import com.mini.ide.core.uicommon.IDEIconButton
import com.mini.ide.core.uicommon.IDETopBar
import com.mini.ide.core.uicommon.Icons
import com.mini.ide.core.uicommon.Typography
import com.mini.ide.engine.api.LogLevel

/**
 * 预览区屏幕 —— 设计说明 §3.3 的 UI 层。
 * 顶部工具条 + 预览 WebView + 可折叠日志面板。
 */
@Composable
fun PreviewScreen(
    projectId: String,
    modifier: Modifier = Modifier,
    viewModel: PreviewViewModel = hiltViewModel(),
) {
    val colors = LocalAppColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var logsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) { viewModel.open(projectId) }

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        IDETopBar(
            title = "预览",
            subtitle = if (state.lastRenderMs > 0) "渲染 ${state.lastRenderMs}ms" else "实时预览",
            actions = {
                IDEIconButton(icon = Icons.Play, onClick = { viewModel.run() }, contentDescription = "运行")
                Spacer(Modifier.width(6.dp))
                IDEIconButton(icon = Icons.Refresh, onClick = { viewModel.reload() }, contentDescription = "刷新")
                Spacer(Modifier.width(6.dp))
                IDEIconButton(icon = Icons.Close, onClick = { viewModel.clearLogs() }, contentDescription = "清空日志")
            }
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PreviewWebView(
                onPortReady = { viewModel.attach(it) },
                onMessage = { type, payload -> viewModel.onBridgeMessage(type, payload) },
                modifier = Modifier.fillMaxSize(),
            )
        }
        LogPanel(
            logs = state.logs,
            expanded = logsExpanded,
            onToggle = { logsExpanded = !logsExpanded },
        )
    }
}

@Composable
private fun LogPanel(
    logs: List<PreviewLog>,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val colors = LocalAppColors.current
    val errorCount = logs.count { it.level == LogLevel.ERROR }
    Column(modifier = Modifier.fillMaxWidth().background(colors.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "控制台 (${logs.size})",
                style = Typography.bodyStrong.copy(color = colors.onBackground),
            )
            if (errorCount > 0) {
                Spacer(Modifier.width(8.dp))
                BasicText(text = "$errorCount 错误", style = Typography.caption.copy(color = colors.error))
            }
            Spacer(Modifier.weight(1f))
            BasicText(
                text = if (expanded) "收起" else "展开",
                style = Typography.caption.copy(color = colors.secondaryText),
            )
        }
        AnimatedVisibility(visible = expanded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .padding(horizontal = 16.dp),
            ) {
                if (logs.isEmpty()) {
                    item { BasicText(text = "暂无日志", style = Typography.caption.copy(color = colors.secondaryText)) }
                }
                items(logs) { log ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        BasicText(
                            text = log.level.name,
                            style = Typography.caption.copy(color = when (log.level) {
                                LogLevel.ERROR -> colors.error
                                LogLevel.WARN -> colors.warning
                                else -> colors.secondaryText
                            }),
                        )
                        Spacer(Modifier.width(8.dp))
                        BasicText(text = log.message, style = Typography.caption.copy(color = colors.onBackground))
                    }
                }
            }
        }
    }
}
