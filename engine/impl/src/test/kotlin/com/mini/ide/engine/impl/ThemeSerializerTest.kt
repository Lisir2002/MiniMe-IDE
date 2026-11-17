package com.mini.ide.engine.impl

import com.mini.ide.core.model.EditorTheme
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeSerializerTest {

    @Test
    fun `ARGB转CSS颜色_正确格式`() {
        assertEquals("#FF0A84FF", ThemeSerializer.argbToCss(0xFF0A84FF))
        assertEquals("#00000000", ThemeSerializer.argbToCss(0x00000000))
        assertEquals("#80ABCDEF", ThemeSerializer.argbToCss(0x80ABCDEF))
    }

    @Test
    fun `主题序列化输出11个CSS变量且值合法`() {
        val json = ThemeSerializer.toJson(EditorTheme.DEFAULT_DARK)
        val obj = JSONObject(json)
        assertEquals(11, obj.length())
        assertEquals("#FF000000", obj.getString("--cm-bg"))
        assertEquals("#FFECECF2", obj.getString("--cm-fg"))
        assertEquals("#FF0A84FF", obj.getString("--cm-cursor"))
        // 所有值都是 #RRGGBBAA 形式
        obj.keys().forEach { key ->
            assertEquals(9, obj.getString(key).length)
            assertEquals('#', obj.getString(key)[0])
        }
    }
}
