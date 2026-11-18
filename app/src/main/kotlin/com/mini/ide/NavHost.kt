package com.mini.ide

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mini.ide.core.designsystem.LocalAppColors
import com.mini.ide.core.uicommon.IDETopBar
import com.mini.ide.core.uicommon.Icons
import com.mini.ide.core.uicommon.Typography
import com.mini.ide.feature.editor.EditorScreen
import com.mini.ide.feature.lesson.LessonScreen
import com.mini.ide.feature.preview.PreviewScreen
import com.mini.ide.feature.project.ProjectScreen

/** 导航目标（设计说明决策 6：不用 navigation-compose，用简单状态）。 */
private sealed interface Screen {
    data object ProjectList : Screen
    data class Workspace(val projectId: String) : Screen
    data class Lessons(val projectId: String) : Screen
}

/** 工作区视图模式。 */
private enum class WorkspaceMode { EDIT, SPLIT, PREVIEW }

/**
 * 应用导航骨架。项目列表 ↔ 工作区；工作区内编辑/预览可切换或分屏。
 */
@Composable
fun MiniMeNavHost() {
    var screen by remember { mutableStateOf<Screen>(Screen.ProjectList) }
    when (val current = screen) {
        Screen.ProjectList -> ProjectScreen(
            onOpenProject = { id -> screen = Screen.Workspace(id) },
        )
        is Screen.Workspace -> WorkspaceScreen(
            projectId = current.projectId,
            onBack = { screen = Screen.ProjectList },
            onOpenLessons = { screen = Screen.Lessons(current.projectId) },
        )
        is Screen.Lessons -> LessonScreen(
            projectId = current.projectId,
            onBack = { screen = Screen.Workspace(current.projectId) },
        )
    }
}

@Composable
private fun WorkspaceScreen(projectId: String, onBack: () -> Unit, onOpenLessons: () -> Unit) {
    var mode by remember { mutableStateOf(WorkspaceMode.SPLIT) }
    Column(modifier = Modifier.fillMaxSize().background(LocalAppColors.current.background)) {
        ModeSwitcher(mode = mode, onModeChange = { mode = it }, onOpenLessons = onOpenLessons)
        when (mode) {
            WorkspaceMode.EDIT -> EditorScreen(projectId = projectId, onBack = onBack, modifier = Modifier.weight(1f))
            WorkspaceMode.PREVIEW -> PreviewScreen(projectId = projectId, modifier = Modifier.weight(1f))
            WorkspaceMode.SPLIT -> SplitView(projectId = projectId, onBack = onBack)
        }
    }
}

@Composable
private fun SplitView(projectId: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 上半：预览（看效果）
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PreviewScreen(projectId = projectId)
        }
        // 分隔线
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LocalAppColors.current.separator)
        )
        // 下半：编辑（写代码）
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            EditorScreen(projectId = projectId, onBack = onBack)
        }
    }
}

@Composable
private fun ModeSwitcher(
    mode: WorkspaceMode,
    onModeChange: (WorkspaceMode) -> Unit,
    onOpenLessons: () -> Unit,
) {
    val colors = LocalAppColors.current
    val items = listOf(
        "编辑" to WorkspaceMode.EDIT,
        "分屏" to WorkspaceMode.SPLIT,
        "预览" to WorkspaceMode.PREVIEW,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(colors.background)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = Modifier.weight(1f)) {
            items.forEachIndexed { index, (label, value) ->
                val selected = mode == value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) colors.primary else colors.surface)
                        .clickable { onModeChange(value) },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = label,
                        style = Typography.caption.copy(color = if (selected) colors.onPrimary else colors.onBackground),
                    )
                }
                if (index < items.lastIndex) Spacer(Modifier.width(8.dp))
            }
        }
        Spacer(Modifier.width(8.dp))
        // 教程入口（设计说明决策 6：常驻可见）
        com.mini.ide.core.uicommon.IDEIconButton(
            icon = Icons.GraduationCap,
            onClick = onOpenLessons,
            contentDescription = "教程",
            modifier = Modifier.height(36.dp),
        )
    }
}
