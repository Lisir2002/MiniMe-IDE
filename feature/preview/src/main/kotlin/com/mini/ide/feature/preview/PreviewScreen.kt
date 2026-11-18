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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mini.ide.core.designsystem.LocalAppColors
import com.mini.ide.core.uicommon.IDEIconButton
import com.mini.ide.core.uicommon.IDETopBar
import com.mini.ide.core.uicommon.Icons
import com.mini.ide.core.uicommon.Typography
import com.mini.ide.engine.api.DiagnosticCategory
import com.mini.ide.engine.api.LogLevel

/**
 * 预览区屏幕 —— M5 错误提示增强：结构化诊断 + 分类过滤 + 教学建议卡片。
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
    // 新增错误时自动展开日志面板
    LaunchedEffect(state.hasNewError) {
        if (state.hasNewError) {
            logsExpanded = true
            viewModel.consumeNewError()
        }
    }

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
            logs = viewModel.filteredLogs(state),
            allLogs = state.logs,
            filter = state.filter,
            expanded = logsExpanded,
            onToggle = { logsExpanded = !logsExpanded },
            onFilter = { viewModel.setFilter(it) },
        )
    }
}

@Composable
private fun LogPanel(
    logs: List<PreviewLog>,
    allLogs: List<PreviewLog>,
    filter: LogFilter,
    expanded: Boolean,
    onToggle: () -> Unit,
    onFilter: (LogFilter) -> Unit,
) {
    val colors = LocalAppColors.current
    val errorCount = allLogs.count { it.level == LogLevel.ERROR }
    val warnCount = allLogs.count { it.level == LogLevel.WARN }
    Column(modifier = Modifier.fillMaxWidth().background(colors.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(text = "控制台 (${allLogs.size})", style = Typography.bodyStrong.copy(color = colors.onBackground))
            if (errorCount > 0) {
                Spacer(Modifier.width(8.dp))
                BasicText(text = "$errorCount 错误", style = Typography.caption.copy(color = colors.error))
            }
            if (warnCount > 0) {
                Spacer(Modifier.width(6.dp))
                BasicText(text = "$warnCount 警告", style = Typography.caption.copy(color = colors.warning))
            }
            Spacer(Modifier.weight(1f))
            BasicText(
                text = if (expanded) "收起" else "展开",
                style = Typography.caption.copy(color = colors.secondaryText),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                // 分类过滤器
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    FilterChip("全部", filter == LogFilter.ALL) { onFilter(LogFilter.ALL) }
                    Spacer(Modifier.width(8.dp))
                    FilterChip("错误", filter == LogFilter.ERRORS) { onFilter(LogFilter.ERRORS) }
                    Spacer(Modifier.width(8.dp))
                    FilterChip("警告", filter == LogFilter.WARNINGS) { onFilter(LogFilter.WARNINGS) }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    if (logs.isEmpty()) {
                        item { BasicText(text = "暂无日志", style = Typography.caption.copy(color = colors.secondaryText)) }
                    }
                    items(logs) { log -> LogRow(log) }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .background(
                if (selected) colors.primary.copy(alpha = 0.25f) else colors.background,
                RoundedCornerShape(8.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        BasicText(
            text = label,
            style = Typography.caption.copy(color = if (selected) colors.primary else colors.secondaryText),
        )
    }
}

@Composable
private fun LogRow(log: PreviewLog) {
    val colors = LocalAppColors.current
    val levelColor = when (log.level) {
        LogLevel.ERROR -> colors.error
        LogLevel.WARN -> colors.warning
        else -> colors.secondaryText
    }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Image(
                painter = rememberVectorPainter(Icons.Error),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(levelColor),
            )
            Spacer(Modifier.width(6.dp))
            val categoryTag = when (log.category) {
                DiagnosticCategory.RUNTIME -> "运行时"
                DiagnosticCategory.SYNTAX -> "语法"
                DiagnosticCategory.NETWORK -> "资源"
                DiagnosticCategory.RESOURCE -> "引用"
                DiagnosticCategory.INFO -> "日志"
                DiagnosticCategory.UNKNOWN -> "提示"
            }
            BasicText(text = "[$categoryTag]", style = Typography.caption.copy(color = levelColor))
            Spacer(Modifier.width(6.dp))
            val head = log.title.ifBlank { log.level.name }
            BasicText(text = head, style = Typography.caption.copy(color = levelColor))
            if (log.line > 0) {
                Spacer(Modifier.width(6.dp))
                BasicText(text = "第${log.line}行", style = Typography.caption.copy(color = colors.secondaryText))
            }
        }
        Spacer(Modifier.height(2.dp))
        BasicText(
            text = log.message,
            style = Typography.caption.copy(color = colors.onBackground),
            modifier = Modifier.padding(start = 20.dp),
        )
        if (log.suggestion != null) {
            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primary.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = rememberVectorPainter(Icons.Lightbulb),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        colorFilter = ColorFilter.tint(colors.primary),
                    )
                    Spacer(Modifier.width(6.dp))
                    BasicText(text = "小提示", style = Typography.caption.copy(color = colors.primary))
                }
                Spacer(Modifier.height(3.dp))
                BasicText(
                    text = log.suggestion!!,
                    style = Typography.caption.copy(color = colors.onBackground),
                )
            }
        }
        if (log.source.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            BasicText(
                text = "来源：${log.source}",
                style = Typography.caption.copy(color = colors.secondaryText),
                modifier = Modifier.padding(start = 20.dp),
            )
        }
    }
}
