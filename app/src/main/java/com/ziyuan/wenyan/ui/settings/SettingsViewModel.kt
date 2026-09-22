package com.ziyuan.wenyan.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.local.AppDatabase
import com.ziyuan.wenyan.util.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// 设置页状态
data class SettingsUiState(
    val fontScale: Float = 1f,
    val themeMode: Int = 0,          // 0=Ore UI 深色，1=Fluent UI 浅色
    val exporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportMessage: String? = null
)

// 单次导出结果（私有）
private data class ExportStatus(val success: Boolean, val message: String)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsStore: SettingsStore,
    private val db: AppDatabase
) : ViewModel() {

    private val exporting = MutableStateFlow(false)
    private val exportStatus = MutableStateFlow<ExportStatus?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsStore.settingsFlow,
        exporting,
        exportStatus
    ) { settings, exportingNow, status ->
        SettingsUiState(
            fontScale = settings.fontScale,
            themeMode = settings.themeMode,
            exporting = exportingNow,
            exportSuccess = status?.success ?: false,
            exportMessage = status?.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    // 调整全 App 字号（拖动即写入）
    fun setFontScale(value: Float) {
        viewModelScope.launch { settingsStore.setFontScale(value) }
    }

    // 切换主题皮肤
    fun setThemeMode(value: Int) {
        viewModelScope.launch { settingsStore.setThemeMode(value) }
    }

    // 导出全部卡片 JSON 并调起系统分享
    fun exportCards() {
        if (exporting.value) return
        viewModelScope.launch {
            exporting.value = true
            exportStatus.value = null
            try {
                val cards = db.cardDao().observeAll().first()
                // dataJson 本身即合法 JSON 对象文本，手动拼接为数组
                val content = "[" + cards.joinToString(",") { it.dataJson } + "]"
                val dir = File(context.cacheDir, "exports").apply { mkdirs() }
                val file = File(dir, "fangkuai_wenyan_cards.json")
                file.writeText(content)
                val uri = FileProvider.getUriForFile(
                    context, "com.ziyuan.wenyan.fileprovider", file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                // 应用上下文启动需 NEW_TASK 标记
                val chooser = Intent.createChooser(intent, "分享卡片数据")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                exportStatus.value = ExportStatus(true, "已导出 ${cards.size} 张卡片")
            } catch (e: Exception) {
                exportStatus.value = ExportStatus(false, "导出失败：${e.message ?: "未知错误"}")
            } finally {
                exporting.value = false
            }
        }
    }
}
