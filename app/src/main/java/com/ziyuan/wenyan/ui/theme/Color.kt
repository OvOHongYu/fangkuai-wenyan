package com.ziyuan.wenyan.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 应用语义配色：一套字段，两套皮肤（Ore UI 深色 / Fluent UI 浅色）
data class AppColors(
    val bgTop: Color,            // 页面背景渐变顶部
    val bgBottom: Color,         // 页面背景渐变底部
    val panel: Color,            // 卡片面板
    val panelBorder: Color,      // 面板边框
    val field: Color,            // 输入框底色
    val fieldBorder: Color,      // 输入框边框
    val trackBg: Color,          // 进度条 / 滑杆轨道底色
    val chipBg: Color,           // 未选中 Chip 底色
    val tabContainer: Color,     // 分段选择器容器底色
    val topBar: Color,           // 顶栏底色
    val navBar: Color,           // 底部导航底色
    val accent: Color,           // 主色（按钮 / 选中）
    val accentPressed: Color,    // 主色按下态
    val accentDim: Color,        // 主色弱化
    val accentDark: Color,       // 实心强调块（序号等）
    val neutral: Color,          // 次按钮
    val neutralPressed: Color,   // 次按钮按下态
    val danger: Color,           // 危险 / 错误
    val warning: Color,          // 收藏星 / 强调
    val textPrimary: Color,      // 主文本
    val textSecondary: Color,    // 次文本
    val onAccent: Color,         // 主色之上的文字色
    val sameHighlight: Color,    // 篇目高亮：当前义项
    val diffHighlight: Color,    // 篇目高亮：其他义项
    val highlightText: Color,    // 篇目高亮之上的文字色
    val isDark: Boolean          // 是否深色（用于状态栏等）
)

// Ore UI 深色（Minecraft 基岩版现代 UI 风格）
val OreDarkColors = AppColors(
    bgTop = Color(0xFF242424),
    bgBottom = Color(0xFF131313),
    panel = Color(0xE6222222),
    panelBorder = Color(0x2EFFFFFF),
    field = Color(0xFF1E1E1E),
    fieldBorder = Color(0xFF3A3A3A),
    trackBg = Color(0xFF2A2A2A),
    chipBg = Color(0xFF2E2E2E),
    tabContainer = Color(0xFF1E1E1E),
    topBar = Color(0xCC1B1B1B),
    navBar = Color(0xF0181818),
    accent = Color(0xFF4EA75A),
    accentPressed = Color(0xFF63C271),
    accentDim = Color(0xFF3D8447),
    accentDark = Color(0xFF2E7D32),
    neutral = Color(0xFF5C5C5C),
    neutralPressed = Color(0xFF6E6E6E),
    danger = Color(0xFFB33A2C),
    warning = Color(0xFFD9A93D),
    textPrimary = Color(0xFFF5F5F5),
    textSecondary = Color(0xFFB8B8B8),
    onAccent = Color.White,
    // 高亮：实心高饱和，绿 / 橙对比强烈
    sameHighlight = Color(0xFF2E7D32),
    diffHighlight = Color(0xFFE65100),
    highlightText = Color.White,
    isDark = true
)

// Fluent UI 浅色（Microsoft Fluent 风格）
val FluentLightColors = AppColors(
    bgTop = Color(0xFFFAFAFA),
    bgBottom = Color(0xFFF0F0F0),
    panel = Color(0xFFFFFFFF),
    panelBorder = Color(0xFFE0E0E0),
    field = Color(0xFFFFFFFF),
    fieldBorder = Color(0xFFC8C8C8),
    trackBg = Color(0xFFE5E5E5),
    chipBg = Color(0xFFF0F0F0),
    tabContainer = Color(0xFFEFEFEF),
    topBar = Color(0xF2F7F7F7),
    navBar = Color(0xF7F3F3F3),
    accent = Color(0xFF0078D4),
    accentPressed = Color(0xFF106EBE),
    accentDim = Color(0xFF005A9E),
    accentDark = Color(0xFF004578),
    neutral = Color(0xFFE4E4E4),
    neutralPressed = Color(0xFFD6D6D6),
    danger = Color(0xFFC42B1C),
    warning = Color(0xFFB8860B),
    textPrimary = Color(0xFF1B1B1B),
    textSecondary = Color(0xFF5F5F5F),
    onAccent = Color.White,
    // 高亮：浅底 + 深字，仍保持绿 / 橙强对比
    sameHighlight = Color(0xFF8FD694),
    diffHighlight = Color(0xFFFFB74D),
    highlightText = Color(0xFF1B1B1B),
    isDark = false
)

val LocalAppColors = staticCompositionLocalOf { OreDarkColors }

// 主题访问入口：AppTheme.colors.accent 等
object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

// 后向兼容别名（渐进替换用，均跟随当前主题）
val OreGreen: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.accent
val OreTextPrimary: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.textPrimary
val OreTextSecondary: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.textSecondary
val OreGray: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.neutral
val OrePanelBorder: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.panelBorder
val OreYellow: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.warning
val OreRed: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.danger
val SameHighlight: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.sameHighlight
val DiffHighlight: Color @Composable @ReadOnlyComposable get() = AppTheme.colors.diffHighlight
