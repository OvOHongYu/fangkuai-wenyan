package com.ziyuan.wenyan.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OreButtonVariant
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreSectionTitle
import com.ziyuan.wenyan.ui.components.OreSlider
import com.ziyuan.wenyan.ui.components.OreTopBar
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.FluentLightColors
import com.ziyuan.wenyan.ui.theme.McFontFamily
import com.ziyuan.wenyan.ui.theme.OreDarkColors
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OreRed
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.theme.PixelFontFamily
import com.ziyuan.wenyan.ui.theme.ThemeMode
import kotlin.math.roundToInt

// 设置（子页面，带返回键）
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OreBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            OreTopBar(
                title = "设置",
                modifier = Modifier.statusBarsPadding(),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(6.dp))

                // ① 主题皮肤
                OreSectionTitle(index = 0, title = "主题皮肤")
                OrePanel(Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            ThemeOption(
                                mode = mode,
                                selected = state.themeMode == ThemeMode.toInt(mode),
                                onClick = { viewModel.setThemeMode(ThemeMode.toInt(mode)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "切换后立即生效，全 App 同步换肤",
                        color = OreTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // ② 字体大小
                OreSectionTitle(index = 1, title = "字体大小")
                OrePanel(Modifier.fillMaxWidth()) {
                    Text("调整全 App 字号", color = OreTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OreSlider(
                            value = state.fontScale,
                            onValueChange = { viewModel.setFontScale(it) },
                            modifier = Modifier.weight(1f),
                            valueRange = 0.8f..1.4f
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "${(state.fontScale * 100).roundToInt()}%",
                            fontFamily = McFontFamily,
                            fontSize = 18.sp,
                            color = OreGreen,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(64.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("小", color = OreTextSecondary, fontSize = 12.sp)
                        Text("标准", color = OreTextPrimary, fontSize = 12.sp)
                        Text("大", color = OreTextSecondary, fontSize = 12.sp)
                    }
                }

                // ③ 导出卡片
                OreSectionTitle(index = 2, title = "导出卡片")
                OrePanel(Modifier.fillMaxWidth()) {
                    Text(
                        text = "将全部卡片数据导出为 JSON 文件并分享",
                        color = OreTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    OreButton(
                        text = "导出并分享",
                        onClick = viewModel::exportCards,
                        modifier = Modifier.fillMaxWidth(),
                        variant = OreButtonVariant.SECONDARY,
                        enabled = !state.exporting
                    )
                    state.exportMessage?.let { message ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = message,
                            color = if (state.exportSuccess) OreGreen else OreRed,
                            fontSize = 13.sp
                        )
                    }
                }

                // ④ 关于
                OreSectionTitle(index = 3, title = "关于")
                OrePanel(Modifier.fillMaxWidth()) {
                    Text(
                        text = "方块文言",
                        color = OreTextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("版本 1.0", color = OreTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "支持 Ore UI（Minecraft 深色）与 Fluent UI（微软浅色）双皮肤",
                        color = OreTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "方舟像素 / Monocraft 英文字体（OFL 许可）· 正文思源黑体",
                        color = OreTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// 主题皮肤选项卡：左侧色板预览 + 名称 + 说明，选中态描边高亮
@Composable
private fun ThemeOption(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    // 直接用该皮肤的调色板做预览色块
    val preview = if (mode == ThemeMode.FLUENT) FluentLightColors else OreDarkColors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) colors.accent.copy(alpha = if (colors.isDark) 0.18f else 0.14f)
                else colors.chipBg
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.accent else colors.panelBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        // 色板预览
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorSwatch(preview.bgTop)
            ColorSwatch(preview.panel)
            ColorSwatch(preview.accent)
            ColorSwatch(preview.warning)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = mode.label,
                color = colors.textPrimary,
                fontFamily = PixelFontFamily,
                fontSize = 14.sp
            )
            if (selected) {
                Spacer(Modifier.width(6.dp))
                Text("✓", color = colors.accent, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = mode.desc,
            color = colors.textSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

// 单个预览色块
@Composable
private fun ColorSwatch(color: androidx.compose.ui.graphics.Color) {
    val colors = AppTheme.colors
    Box(
        Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .border(1.dp, colors.panelBorder, RoundedCornerShape(4.dp))
    )
}
