package com.mini.ide.core.uicommon

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 排版令牌 —— 对应视觉规范 §4 字号/字重阶梯。
 * 字体族：UI 用系统默认（Inter 风格），代码区用等宽（由编辑器 WebView 承担）。
 */
object Typography {
    val display = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.W700, lineHeight = 41.sp)
    val title = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.W600, lineHeight = 28.sp)
    val body = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.W400, lineHeight = 22.sp)
    val bodyStrong = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.W600, lineHeight = 22.sp)
    val caption = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.W400, lineHeight = 18.sp)
    val code = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W400, lineHeight = 20.sp)
}
