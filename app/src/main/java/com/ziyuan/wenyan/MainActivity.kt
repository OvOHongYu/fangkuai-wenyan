package com.ziyuan.wenyan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.ui.MainScreen
import com.ziyuan.wenyan.ui.theme.ThemeMode
import com.ziyuan.wenyan.ui.theme.WenyanTheme
import com.ziyuan.wenyan.util.SettingsStore
import com.ziyuan.wenyan.util.UserSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsStore.settingsFlow.collectAsStateWithLifecycle(initialValue = UserSettings())
            // 皮肤由设置决定：Ore UI 深色 / Fluent UI 浅色；字号可缩放
            WenyanTheme(mode = ThemeMode.fromInt(settings.themeMode)) {
                val density = Density(density = LocalDensity.current.density, fontScale = settings.fontScale)
                CompositionLocalProvider(LocalDensity provides density) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        MainScreen()
                    }
                }
            }
        }
    }
}
