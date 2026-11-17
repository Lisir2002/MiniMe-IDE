package com.mini.ide.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** M0 契约测试：验证全局常量的稳定性与相互约束。 */
class MiniMeConstantsTest {

    @Test
    fun `应用标识固定为 MiniMe-IDE`() {
        assertEquals("MiniMe-IDE", MiniMeConstants.APP_NAME)
    }

    @Test
    fun `预览防抖必须比自动保存更灵敏`() {
        // 预览要快速反馈，自动保存可稍慢以减少写盘，这是数据流设计的前提
        assertTrue(
            "预览防抖(${MiniMeConstants.PREVIEW_DEBOUNCE_MS}) 应小于自动保存(${MiniMeConstants.AUTOSAVE_DEBOUNCE_MS})",
            MiniMeConstants.PREVIEW_DEBOUNCE_MS < MiniMeConstants.AUTOSAVE_DEBOUNCE_MS,
        )
    }

    @Test
    fun `防抖时长为正数且单文件大小上限合理`() {
        assertTrue(MiniMeConstants.PREVIEW_DEBOUNCE_MS > 0)
        assertTrue(MiniMeConstants.AUTOSAVE_DEBOUNCE_MS > 0)
        assertTrue(MiniMeConstants.MAX_FILE_SIZE_KB in 1..4096)
    }
}
