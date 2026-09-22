package com.ziyuan.wenyan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// 皮肤模式：Ore UI 深色 / Fluent UI 浅色
enum class ThemeMode(val label: String, val desc: String) {
    ORE("Ore UI", "Minecraft 风格 · 深色"),
    FLUENT("Fluent UI", "微软风格 · 浅色");

    companion object {
        fun fromInt(value: Int): ThemeMode = entries.getOrElse(value) { ORE }
        fun toInt(mode: ThemeMode): Int = mode.ordinal
    }
}

private fun oreMaterialScheme(colors: AppColors) = darkColorScheme(
    primary = colors.accent,
    onPrimary = colors.onAccent,
    primaryContainer = colors.accentDim,
    onPrimaryContainer = colors.onAccent,
    secondary = colors.neutral,
    onSecondary = Color.White,
    secondaryContainer = colors.neutralPressed,
    onSecondaryContainer = Color.White,
    tertiary = colors.warning,
    onTertiary = Color(0xFF2A2008),
    background = colors.bgBottom,
    onBackground = colors.textPrimary,
    surface = colors.panel,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.chipBg,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.fieldBorder,
    error = colors.danger,
    onError = Color.White
)

private fun fluentMaterialScheme(colors: AppColors) = lightColorScheme(
    primary = colors.accent,
    onPrimary = colors.onAccent,
    primaryContainer = Color(0xFFD3E8F8),
    onPrimaryContainer = colors.accentDark,
    secondary = colors.neutral,
    onSecondary = colors.textPrimary,
    secondaryContainer = colors.chipBg,
    onSecondaryContainer = colors.textPrimary,
    tertiary = colors.warning,
    onTertiary = Color.White,
    background = colors.bgBottom,
    onBackground = colors.textPrimary,
    surface = colors.panel,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.chipBg,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.fieldBorder,
    error = colors.danger,
    onError = Color.White
)

@Composable
fun WenyanTheme(mode: ThemeMode = ThemeMode.ORE, content: @Composable () -> Unit) {
    val colors = if (mode == ThemeMode.FLUENT) FluentLightColors else OreDarkColors
    val scheme = if (mode == ThemeMode.FLUENT) fluentMaterialScheme(colors) else oreMaterialScheme(colors)
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}
