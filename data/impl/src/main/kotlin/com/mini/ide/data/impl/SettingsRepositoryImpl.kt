package com.mini.ide.data.impl

import android.content.Context
import android.content.SharedPreferences
import com.mini.ide.core.model.Settings
import com.mini.ide.core.model.ThemeMode
import com.mini.ide.data.api.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 设置仓库实现。MVP 用 SharedPreferences + 内存 StateFlow。
 * 读改写经 Mutex 串行化，保证 transform 原子性；未来可换 DataStore 而不动契约。
 */
class SettingsRepositoryImpl(context: Context) : SettingsRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("minime_settings", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val state = MutableStateFlow(load())

    override fun observe(): Flow<Settings> = state.asStateFlow()

    override suspend fun update(transform: (Settings) -> Settings) = mutex.withLock {
        val next = transform(state.value)
        persist(next)
        state.value = next
    }

    private fun load(): Settings = Settings(
        themeMode = runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, "DARK")!!) }
            .getOrDefault(ThemeMode.DARK),
        editorFontSizeSp = prefs.getInt(KEY_FONT_SIZE, 14),
        wordWrap = prefs.getBoolean(KEY_WORD_WRAP, false),
        previewDebounceMs = prefs.getLong(KEY_PREVIEW_DEBOUNCE, 300L),
        autoSave = prefs.getBoolean(KEY_AUTOSAVE, true),
        sandboxMode = prefs.getBoolean(KEY_SANDBOX, true),
    )

    private fun persist(s: Settings) {
        prefs.edit().apply {
            putString(KEY_THEME, s.themeMode.name)
            putInt(KEY_FONT_SIZE, s.editorFontSizeSp)
            putBoolean(KEY_WORD_WRAP, s.wordWrap)
            putLong(KEY_PREVIEW_DEBOUNCE, s.previewDebounceMs)
            putBoolean(KEY_AUTOSAVE, s.autoSave)
            putBoolean(KEY_SANDBOX, s.sandboxMode)
            apply()
        }
    }

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_WORD_WRAP = "word_wrap"
        const val KEY_PREVIEW_DEBOUNCE = "preview_debounce"
        const val KEY_AUTOSAVE = "autosave"
        const val KEY_SANDBOX = "sandbox"
    }
}
