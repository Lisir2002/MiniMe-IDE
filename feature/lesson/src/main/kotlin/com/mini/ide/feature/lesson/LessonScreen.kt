package com.mini.ide.feature.lesson

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.mini.ide.core.uicommon.IDEPrimaryButton
import com.mini.ide.core.uicommon.IDESecondaryButton
import com.mini.ide.core.uicommon.IDETopBar
import com.mini.ide.core.uicommon.Icons
import com.mini.ide.core.uicommon.Typography

/**
 * 教学屏幕（M5 激活）—— 课程列表 ↔ 课程详情分步任务。
 * 全自绘组件，遵循视觉规范。校验需当前项目文件，故接收 projectId。
 */
@Composable
fun LessonScreen(
    projectId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LessonViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var openLessonId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadLessons() }

    val current = state.loaded
    when {
        openLessonId != null && current != null && current.lesson.id == openLessonId ->
            LessonDetail(
                state = state,
                projectId = projectId,
                onBack = { openLessonId = null },
                onCheck = { viewModel.checkStep(projectId) },
                onNext = { viewModel.nextStep() },
                onToggleHint = { viewModel.toggleHint() },
                onReset = { viewModel.reset(current.lesson.id) },
                modifier = modifier,
            )
        else -> LessonList(
            lessons = state.lessons,
            enabled = state.lessonEnabled,
            onOpen = { id -> openLessonId = id; viewModel.openLesson(id) },
            onEnable = { viewModel.enableLessons() },
            onBack = onBack,
            modifier = modifier,
        )
    }
}

@Composable
private fun LessonList(
    lessons: List<com.mini.ide.core.model.Lesson>,
    enabled: Boolean,
    onOpen: (String) -> Unit,
    onEnable: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val colors = LocalAppColors.current
    var showEnableCard by remember { mutableStateOf(!enabled) }
    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        IDETopBar(
            title = "教程",
            subtitle = if (enabled) "分步任务，边学边做" else "启用后开始学习",
            navIcon = Icons.Back,
            onNavClick = onBack,
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
            if (showEnableCard) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surface, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                            .padding(16.dp),
                    ) {
                        BasicText(text = "开启教学模式", style = Typography.title.copy(color = colors.onBackground))
                        Spacer(Modifier.height(6.dp))
                        BasicText(
                            text = "教程会一步步带你写代码，并自动检查每一步是否完成。",
                            style = Typography.body.copy(color = colors.secondaryText),
                        )
                        Spacer(Modifier.height(12.dp))
                        IDEPrimaryButton(
                            text = "启用",
                            icon = Icons.GraduationCap,
                            onClick = { onEnable(); showEnableCard = false },
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
            itemsIndexed(lessons) { _, lesson ->
                com.mini.ide.core.uicommon.IDECard(
                    onClick = { if (enabled) onOpen(lesson.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Book),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(colors.primary),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(text = lesson.title, style = Typography.bodyStrong.copy(color = colors.onBackground))
                            BasicText(text = lesson.description, style = Typography.caption.copy(color = colors.secondaryText))
                        }
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.ChevronRight),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(colors.secondaryText),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun LessonDetail(
    state: LessonUiState,
    projectId: String,
    onBack: () -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    onToggleHint: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier,
) {
    val colors = LocalAppColors.current
    val loaded = state.loaded!!
    val lesson = loaded.lesson
    val step = loaded.currentStep
    val total = lesson.steps.size
    val done = loaded.progress.completedStepIds.size
    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        IDETopBar(
            title = lesson.title,
            subtitle = "已完成 $done / $total 步",
            navIcon = Icons.Back,
            onNavClick = onBack,
            actions = {
                com.mini.ide.core.uicommon.IDEIconButton(icon = Icons.Refresh, onClick = onReset, contentDescription = "重置进度")
            },
        )
        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(colors.separator),
        ) {
            if (total > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(done.toFloat() / total)
                        .height(4.dp)
                        .background(colors.primary),
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            item {
                if (step == null) {
                    BasicText(
                        text = "全部步骤已完成！回到项目里看看你的作品吧。",
                        style = Typography.bodyStrong.copy(color = colors.success),
                    )
                } else {
                    val idx = loaded.progress.currentStepIndex + 1
                    BasicText(text = "第 $idx 步 / 共 $total 步", style = Typography.caption.copy(color = colors.primary))
                    Spacer(Modifier.height(8.dp))
                    BasicText(text = step.instruction, style = Typography.title.copy(color = colors.onBackground))
                    Spacer(Modifier.height(16.dp))
                }
            }
            // 提示卡片
            if (step?.hint != null) {
                item {
                    IDESecondaryButton(
                        text = if (state.hintVisible) "收起提示" else "需要提示？",
                        icon = Icons.Lightbulb,
                        onClick = onToggleHint,
                    )
                    AnimatedVisibility(visible = state.hintVisible) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(colors.surface, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                                .padding(14.dp),
                        ) {
                            BasicText(text = step.hint!!, style = Typography.body.copy(color = colors.warning))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            // 校验结果
            state.checkResult?.let { result ->
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(
                                if (result.passed) Icons.CheckCircle else Icons.Error
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                if (result.passed) colors.success else colors.error
                            ),
                        )
                        Spacer(Modifier.width(8.dp))
                        BasicText(
                            text = result.feedback,
                            style = Typography.body.copy(color = if (result.passed) colors.success else colors.error),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            // 操作按钮
            if (step != null) {
                item {
                    Row {
                        IDESecondaryButton(text = "检查这一步", icon = Icons.CheckCircle, onClick = onCheck)
                        Spacer(Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            IDEPrimaryButton(
                                text = "下一步",
                                onClick = onNext,
                                enabled = state.checkResult?.passed == true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    BasicText(
                        text = "提示：先在工作区按步骤改好代码，再回来点「检查这一步」。",
                        style = Typography.caption.copy(color = colors.secondaryText),
                    )
                }
            }
        }
    }
}
