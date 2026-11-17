package com.mini.ide.core.uicommon

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.SpringSpec

/**
 * 动效规格 —— 对应视觉规范 §5.3。spring 物理动画是 iOS"丝滑"的核心。
 * 三条原则：有因果、不抢戏、可打断。
 */
object Motion {
    /** 标准 spring（通用、稳重不弹）：按钮按压、状态切换。 */
    val DampingNoOvershoot: SpringSpec<Float> = spring(
        dampingRatio = 1.0f,
        stiffness = 300f,
    )

    /** 轻弹（按钮、卡片出现）。 */
    val Spring: SpringSpec<Float> = spring(
        dampingRatio = 0.8f,
        stiffness = 280f,
    )

    /** 抽屉/模态（略带弹性的入场）。 */
    val Bouncy: SpringSpec<Float> = spring(
        dampingRatio = 0.6f,
        stiffness = 260f,
    )

    /** 时长阶梯（ms）—— 对应视觉规范 §5.2。 */
    object Duration {
        const val MICRO = 130       // 微交互：按钮按下、选中
        const val COMPONENT = 230   // 组件状态：展开/收起、颜色变化
        const val TRANSITION = 330  // 页面转场
        const val SCENE = 400       // 抽屉、模态
    }
}
