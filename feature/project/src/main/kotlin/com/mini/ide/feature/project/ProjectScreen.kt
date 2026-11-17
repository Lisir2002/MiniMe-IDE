package com.mini.ide.feature.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mini.ide.core.designsystem.LocalAppColors
import com.mini.ide.core.model.Project
import com.mini.ide.core.uicommon.IDEIconButton
import com.mini.ide.core.uicommon.IDEPrimaryButton
import com.mini.ide.core.uicommon.IDETopBar
import com.mini.ide.core.uicommon.Icons
import com.mini.ide.core.uicommon.Typography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 项目列表屏幕 —— 设计说明 §3.1 的 UI 层。
 * 通过 onOpenProject 上抛导航意图，自身不感知导航实现。
 */
@Composable
fun ProjectScreen(
    onOpenProject: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.openedProject.collect { id -> onOpenProject(id) }
    }
    LaunchedEffect(Unit) { viewModel.load() }

    val dateFmt = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())

    Column(modifier = modifier.fillMaxSize()) {
        IDETopBar(
            title = "MiniMe",
            subtitle = "我的作品",
            actions = {
                IDEIconButton(icon = Icons.Plus, onClick = { viewModel.create("未命名项目") }, contentDescription = "新建项目")
            }
        )
        if (state.projects.isEmpty() && !state.isLoading) {
            EmptyState(onCreate = { viewModel.create("我的第一个作品") })
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        dateText = dateFmt.format(Date(project.updatedAt)),
                        onClick = { viewModel.open(project.id) },
                        onDelete = { viewModel.delete(project.id) },
                    )
                }
            }
        }
        Box(Modifier.padding(16.dp)) {
            IDEPrimaryButton(
                text = "新建作品",
                onClick = { viewModel.create("未命名项目") },
                icon = Icons.Plus,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    dateText: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalAppColors.current
    com.mini.ide.core.uicommon.IDECard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IDEIconButton(icon = Icons.Folder, onClick = onClick, contentDescription = "项目")
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                BasicText(text = project.name, style = Typography.bodyStrong.copy(color = colors.onBackground), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                BasicText(text = "更新于 $dateText", style = Typography.caption.copy(color = colors.secondaryText))
            }
            IDEIconButton(icon = Icons.Trash, onClick = onDelete, contentDescription = "删除项目", tint = colors.error)
        }
    }
}

@Composable
private fun EmptyState(onCreate: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicText(text = "还没有作品", style = Typography.title.copy(color = colors.onBackground))
        Spacer(Modifier.height(8.dp))
        BasicText(
            text = "点击下方按钮，创建你的第一个网页作品，边写代码边看效果。",
            style = Typography.body.copy(color = colors.secondaryText),
        )
    }
}
