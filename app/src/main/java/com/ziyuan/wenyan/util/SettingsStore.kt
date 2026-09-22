package com.ziyuan.wenyan.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// 用户设置：字体缩放 + 主题皮肤（0=Ore UI 深色，1=Fluent UI 浅色）
data class UserSettings(val fontScale: Float = 1f, val themeMode: Int = 0)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsStore @Inject constructor(@ApplicationContext private val context: Context) {

    val settingsFlow: Flow<UserSettings> = context.settingsDataStore.data.map { prefs ->
        UserSettings(
            fontScale = prefs[KEY_FONT_SCALE] ?: 1f,
            themeMode = prefs[KEY_THEME_MODE] ?: 0
        )
    }

    suspend fun setFontScale(value: Float) {
        context.settingsDataStore.edit { it[KEY_FONT_SCALE] = value }
    }

    suspend fun setThemeMode(value: Int) {
        context.settingsDataStore.edit { it[KEY_THEME_MODE] = value }
    }

    companion object {
        private val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
        private val KEY_THEME_MODE = intPreferencesKey("theme_mode")
    }
}
