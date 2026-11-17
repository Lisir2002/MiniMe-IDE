package com.mini.ide.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mini.ide.core.designsystem.LocalAppColors
import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.uicommon.IDEIconButton
import com.mini.ide.core.uicommon.IDETopBar
import com.mini.ide.core.uicommon.Icons
import com.mini.ide.core.uicommon.Typography

/**
 * 编辑区屏幕 —— 设计说明 §3.2 的 UI 层。
 * 顶部文件 Tab + 编辑 WebView + 底部工具栏。预览由 app 层并列放置。
 */
@Composable
fun EditorScreen(
    projectId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val colors = LocalAppColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) { viewModel.open(projectId) }

    Column(modifier = modifier.fillMaxSize()) {
        IDETopBar(
            title = state.projectName.ifBlank { "编辑" },
            navIcon = Icons.Back,
            onNavClick = { viewModel.save(); onBack() },
            subtitle = if (state.isDirty) "编辑中 · 未保存" else "已保存",
            actions = {
                IDEIconButton(icon = Icons.Save, onClick = { viewModel.save() }, contentDescription = "保存")
            }
        )
        FileTabs(
            files = state.files,
            activeId = state.activeFileId,
            onSelect = { viewModel.switchFile(it) },
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            EditorWebView(onPortReady = { viewModel.attach(it) }, modifier = Modifier.fillMaxSize())
        }
        EditorToolbar(
            line = state.line,
            column = state.column,
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onSnippet = { viewModel.insert("<div>Hello MiniMe</div>") },
        )
    }
}

@Composable
private fun FileTabs(
    files: List<SourceFile>,
    activeId: String,
    onSelect: (String) -> Unit,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(colors.surface)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        files.forEach { file ->
            val selected = file.id == activeId
            val icon = when (file.kind) {
                FileKind.HTML -> Icons.FileHtml
                FileKind.CSS -> Icons.FileCss
                FileKind.JS -> Icons.FileJs
                FileKind.DSL -> Icons.FileHtml
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) colors.primary.copy(alpha = 0.18f) else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onSelect(file.id) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(icon),
                    contentDescription = file.name,
                    modifier = Modifier.height(16.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (selected) colors.primary else colors.secondaryText),
                )
                Spacer(Modifier.width(6.dp))
                BasicText(text = file.name, style = Typography.caption.copy(color = if (selected) colors.primary else colors.secondaryText))
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun EditorToolbar(
    line: Int,
    column: Int,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSnippet: () -> Unit,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(colors.surface)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IDEIconButton(icon = Icons.Undo, onClick = onUndo, contentDescription = "撤销")
        Spacer(Modifier.width(8.dp))
        IDEIconButton(icon = Icons.Redo, onClick = onRedo, contentDescription = "重做")
        Spacer(Modifier.width(8.dp))
        IDEIconButton(icon = Icons.Plus, onClick = onSnippet, contentDescription = "插入片段")
        Spacer(Modifier.weight(1f))
        BasicText(text = "行 $line · 列 $column", style = Typography.caption.copy(color = colors.secondaryText))
    }
}
