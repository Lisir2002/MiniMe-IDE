package com.mini.ide.core.model

/**
 * S6 教学系统领域模型（M5 激活）。
 *
 * 依赖约束：本文件处于 core:model 最底层，不引用任何上层类型。
 * 蓝图中 Lesson.target 原为 engine:api 的 PreviewDocument，此处改为纯数据
 * [LessonTarget]（见 M5 设计说明决策 1），避免 core 反向依赖 engine。
 */

/** 教学用校验规则。数据驱动，避免硬编码到引擎。 */
sealed interface ValidatorSpec {
    /** 三段源码拼接文本包含指定子串即通过。 */
    data class Contains(val text: String) : ValidatorSpec

    /** 三段源码拼接文本匹配正则即通过。 */
    data class Regex(val pattern: String, val ignoreCase: Boolean = true) : ValidatorSpec

    /**
     * 结构启发式校验（Phase2 无真 AST，降级实现）。
     * rule 为预定义规则名，由 StepValidator 解释；文档标注为"教学向宽松校验"。
     */
    data class AstRule(val rule: String) : ValidatorSpec
}

/** 课程目标的纯数据快照（用于"目标效果对照"，不含 WebView / PreviewDocument）。 */
data class LessonTarget(
    val html: String = "",
    val css: String = "",
    val js: String = "",
    val description: String = "",
)

/** 一个分步任务。 */
data class LessonStep(
    val id: String,
    val instruction: String,
    val hint: String? = null,
    val validator: ValidatorSpec,
)

/** 一节课程。 */
data class Lesson(
    val id: String,
    val title: String,
    val description: String = "",
    val steps: List<LessonStep> = emptyList(),
    val target: LessonTarget = LessonTarget(),
)

/** 校验上下文：当前项目的三文件源码。 */
data class CheckContext(val files: List<SourceFile>)

/** 单步校验结果。 */
data class StepResult(val passed: Boolean, val feedback: String)

/** 课程进度（每课程一条，独立持久化，见设计说明决策 2）。 */
data class LessonProgress(
    val lessonId: String,
    val currentStepIndex: Int = 0,
    val completedStepIds: Set<String> = emptySet(),
    val startedAt: Long = 0L,
    val updatedAt: Long = 0L,
)

/** 引擎对外暴露的课程运行状态。 */
data class LessonState(
    val lesson: Lesson,
    val progress: LessonProgress,
    val currentStep: LessonStep?,
)
