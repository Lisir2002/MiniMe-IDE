package com.mini.ide

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mini.ide.core.designsystem.LocalAppColors

/**
 * 导航骨架。M0 只有一个占位首页；M1+ 接入 project/editor/preview。
 * 刻意用 foundation 的 BasicText，避免引入 material3，保持脚手架最小编译面。
 */
@Composable
fun MiniMeNavHost() {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text = "MiniMe-IDE", color = { colors.onBackground })
    }
}
