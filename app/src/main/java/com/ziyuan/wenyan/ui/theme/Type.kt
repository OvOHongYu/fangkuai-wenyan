package com.ziyuan.wenyan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 字体分层：标题/按钮等醒目元素用像素字体；正文/长文本用系统默认（Noto Sans CJK 思源黑体）
val Typography = Typography(
    displaySmall = TextStyle(fontFamily = PixelFontFamily, fontWeight = FontWeight.Normal, fontSize = 30.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = PixelFontFamily, fontWeight = FontWeight.Normal, fontSize = 26.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontFamily = PixelFontFamily, fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = PixelFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontSize = 14.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontFamily = PixelFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontSize = 12.sp, lineHeight = 18.sp)
)
