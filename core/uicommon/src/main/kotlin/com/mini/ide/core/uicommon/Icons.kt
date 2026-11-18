package com.mini.ide.core.uicommon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 语义化自绘图标 —— 对应视觉规范 §6。
 * 24×24 网格、2dp 描边、圆头圆角（iOS SF Symbols 观感）。
 * 颜色用 currentColor 语义：由调用方传 tint，不写死色值。
 * 图标含义与位置强绑定（如"运行"用 play，"保存"用下箭头落盘）。
 */
object Icons {
    private val stroke = SolidColor(Color.White) // 占位色，调用方用 tint 覆盖
    private fun builder(name: String) = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    private fun ImageVector.Builder.pathStroke(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) =
        path(
            stroke = stroke,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        )

    val Back: ImageVector = builder("ic_back").apply {
        pathStroke { moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f) }
    }.build()

    val Close: ImageVector = builder("ic_close").apply {
        pathStroke { moveTo(6f, 6f); lineTo(18f, 18f); moveTo(18f, 6f); lineTo(6f, 18f) }
    }.build()

    val Plus: ImageVector = builder("ic_plus").apply {
        pathStroke { moveTo(12f, 5f); lineTo(12f, 19f); moveTo(5f, 12f); lineTo(19f, 12f) }
    }.build()

    val Folder: ImageVector = builder("ic_folder").apply {
        pathStroke {
            moveTo(3f, 7f); lineTo(3f, 18f); lineTo(21f, 18f); lineTo(21f, 9f)
            lineTo(12f, 9f); lineTo(9f, 6f); lineTo(3f, 6f); close()
        }
    }.build()

    val Play: ImageVector = builder("ic_play").apply {
        path(
            fill = stroke,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) { moveTo(8f, 5f); lineTo(19f, 12f); lineTo(8f, 19f); close() }
    }.build()

    val Refresh: ImageVector = builder("ic_refresh").apply {
        pathStroke {
            moveTo(20f, 11f); curveTo(19.2f, 6.5f, 15.4f, 4f, 11f, 4f)
            curveTo(6f, 4f, 3f, 7.5f, 3f, 12f); curveTo(3f, 16.5f, 6f, 20f, 11f, 20f)
            curveTo(15f, 20f, 18f, 17.5f, 19f, 14f)
        }
        pathStroke { moveTo(19f, 8f); lineTo(19f, 14f); lineTo(13f, 14f) }
    }.build()

    val Undo: ImageVector = builder("ic_undo").apply {
        pathStroke { moveTo(9f, 8f); lineTo(5f, 12f); lineTo(9f, 16f) }
        pathStroke {
            moveTo(5f, 12f); lineTo(14f, 12f)
            curveTo(18f, 12f, 19f, 14f, 19f, 18f)
        }
    }.build()

    val Redo: ImageVector = builder("ic_redo").apply {
        pathStroke { moveTo(15f, 8f); lineTo(19f, 12f); lineTo(15f, 16f) }
        pathStroke {
            moveTo(19f, 12f); lineTo(10f, 12f)
            curveTo(6f, 12f, 5f, 14f, 5f, 18f)
        }
    }.build()

    val Save: ImageVector = builder("ic_save").apply {
        pathStroke { moveTo(12f, 4f); lineTo(12f, 16f) }
        pathStroke { moveTo(7f, 11f); lineTo(12f, 16f); lineTo(17f, 11f) }
        pathStroke { moveTo(5f, 20f); lineTo(19f, 20f) }
    }.build()

    val Trash: ImageVector = builder("ic_trash").apply {
        pathStroke { moveTo(5f, 7f); lineTo(19f, 7f) }
        pathStroke { moveTo(9f, 7f); lineTo(9f, 5f); lineTo(15f, 5f); lineTo(15f, 7f) }
        pathStroke {
            moveTo(7f, 7f); lineTo(8f, 19f); lineTo(16f, 19f); lineTo(17f, 7f)
        }
    }.build()

    val Moon: ImageVector = builder("ic_theme").apply {
        pathStroke {
            moveTo(20f, 14f)
            curveTo(18f, 18f, 13f, 20f, 9f, 18f)
            curveTo(5f, 16f, 4f, 11f, 7f, 7f)
            curveTo(8f, 12f, 14f, 15f, 20f, 14f)
            close()
        }
    }.build()

    val Error: ImageVector = builder("ic_error").apply {
        pathStroke { moveTo(12f, 7f); lineTo(12f, 13f) }
        pathStroke { moveTo(12f, 17f); lineTo(12f, 17.01f) }
        pathStroke { moveTo(12f, 3f); curveTo(5f, 3f, 2f, 9f, 5f, 15f); curveTo(8f, 21f, 16f, 21f, 19f, 15f); curveTo(22f, 9f, 19f, 3f, 12f, 3f); close() }
    }.build()

    val ChevronRight: ImageVector = builder("ic_chevron_right").apply {
        pathStroke { moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f) }
    }.build()

    /** 文件类型图标：用差异化角标让新手一眼分辨。letter 由调用方叠加文本，这里给外形。 */
    val FileHtml: ImageVector = builder("ic_file_html").apply {
        pathStroke {
            moveTo(6f, 3f); lineTo(14f, 3f); lineTo(18f, 7f); lineTo(18f, 21f)
            lineTo(6f, 21f); close()
        }
        pathStroke { moveTo(14f, 3f); lineTo(14f, 7f); lineTo(18f, 7f) }
    }.build()

    val FileCss = FileHtml
    val FileJs = FileHtml

    /** M5 教学语义图标。 */

    /** 学士帽：教程入口。 */
    val GraduationCap: ImageVector = builder("ic_graduation_cap").apply {
        pathStroke { moveTo(2f, 9f); lineTo(12f, 5f); lineTo(22f, 9f); lineTo(12f, 13f); close() }
        pathStroke { moveTo(6f, 11f); lineTo(6f, 16f); curveTo(9f, 19f, 15f, 19f, 18f, 16f); lineTo(18f, 11f) }
        pathStroke { moveTo(22f, 9f); lineTo(22f, 15f) }
    }.build()

    /** 灯泡：提示卡片。 */
    val Lightbulb: ImageVector = builder("ic_lightbulb").apply {
        pathStroke {
            moveTo(9f, 18f); lineTo(15f, 18f)
            moveTo(10f, 21f); lineTo(14f, 21f)
        }
        pathStroke {
            moveTo(12f, 3f)
            curveTo(7.5f, 3f, 5f, 6.5f, 5f, 10f)
            curveTo(5f, 13f, 7f, 14.5f, 8f, 17f)
            lineTo(16f, 17f)
            curveTo(17f, 14.5f, 19f, 13f, 19f, 10f)
            curveTo(19f, 6.5f, 16.5f, 3f, 12f, 3f)
            close()
        }
    }.build()

    /** 勾选圆：通过/完成。 */
    val CheckCircle: ImageVector = builder("ic_check_circle").apply {
        pathStroke { moveTo(8f, 12f); lineTo(11f, 15f); lineTo(16f, 9f) }
        pathStroke {
            moveTo(12f, 3f)
            curveTo(7f, 3f, 3f, 7f, 3f, 12f)
            curveTo(3f, 17f, 7f, 21f, 12f, 21f)
            curveTo(17f, 21f, 21f, 17f, 21f, 12f)
            curveTo(21f, 7f, 17f, 3f, 12f, 3f)
            close()
        }
    }.build()

    /** 书本：课程详情。 */
    val Book: ImageVector = builder("ic_book").apply {
        pathStroke {
            moveTo(12f, 6f)
            curveTo(9.5f, 4.5f, 6f, 4.5f, 3f, 5.5f)
            lineTo(3f, 18.5f)
            curveTo(6f, 17.5f, 9.5f, 17.5f, 12f, 19f)
            curveTo(14.5f, 17.5f, 18f, 17.5f, 21f, 18.5f)
            lineTo(21f, 5.5f)
            curveTo(18f, 4.5f, 14.5f, 4.5f, 12f, 6f)
            close()
        }
        pathStroke { moveTo(12f, 6f); lineTo(12f, 19f) }
    }.build()
}
