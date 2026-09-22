package com.ziyuan.wenyan.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.PixelFontFamily

// ===== 共享组件库（跟随当前皮肤：Ore UI 深色 / Fluent UI 浅色） =====

// 全屏背景：渐变
@Composable
fun OreBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.bgTop, colors.bgBottom))),
        content = content
    )
}

// 面板：卡片容器
@Composable
fun OrePanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.panel)
            .border(1.dp, colors.panelBorder, RoundedCornerShape(10.dp))
            .padding(14.dp),
        content = content
    )
}

// 按钮：PRIMARY 主色 / SECONDARY 中性 / DANGER 危险
enum class OreButtonVariant { PRIMARY, SECONDARY, DANGER }

@Composable
fun OreButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: OreButtonVariant = OreButtonVariant.PRIMARY,
    enabled: Boolean = true
) {
    val colors = AppTheme.colors
    val fill = when (variant) {
        OreButtonVariant.PRIMARY -> colors.accent
        OreButtonVariant.SECONDARY -> colors.neutral
        OreButtonVariant.DANGER -> colors.danger
    }
    // 浅色中性按钮上的文字用主文本色，保证对比度
    val contentColor =
        if (variant == OreButtonVariant.SECONDARY && !colors.isDark) colors.textPrimary else colors.onAccent
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = fill,
            contentColor = contentColor,
            disabledContainerColor = fill.copy(alpha = if (colors.isDark) 0.35f else 0.45f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            fontFamily = PixelFontFamily,
            fontSize = 15.sp
        )
    }
}

// 标签 Chip：选中态主色填充
@Composable
fun OreChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) colors.accent else colors.chipBg)
            .border(
                1.dp,
                if (selected) Color.Transparent else colors.panelBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = if (selected) colors.onAccent else colors.textSecondary,
            fontSize = 13.sp
        )
    }
}

// 分段式进度条
@Composable
fun OreProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(colors.trackBg)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .width(maxWidth * progress.coerceIn(0f, 1f))
                .background(colors.accent)
        )
        val gapColor = colors.bgBottom.copy(alpha = 0.55f)
        Canvas(Modifier.fillMaxSize()) {
            val step = 12.dp.toPx()
            var x = step
            while (x < size.width) {
                drawLine(
                    color = gapColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2.dp.toPx()
                )
                x += step
            }
        }
    }
}

// 设置项滑杆
@Composable
fun OreSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0
) {
    val colors = AppTheme.colors
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        steps = steps,
        colors = SliderDefaults.colors(
            thumbColor = colors.accent,
            activeTrackColor = colors.accent,
            inactiveTrackColor = colors.trackBg
        )
    )
}

// 开关
@Composable
fun OreSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedTrackColor = colors.accent,
            checkedThumbColor = colors.onAccent,
            uncheckedTrackColor = colors.trackBg,
            uncheckedThumbColor = colors.textSecondary
        )
    )
}

// 顶栏：56dp + 返回箭头 + 底部 1dp 分隔线
@Composable
fun OreTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colors = AppTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(colors.topBar)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Text(
                    text = "←",
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Text(
                text = title,
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            actions()
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.panelBorder)
        )
    }
}

// 输入框
@Composable
fun OreTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    val colors = AppTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder, color = colors.textSecondary) },
        singleLine = singleLine,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.field,
            unfocusedContainerColor = colors.field,
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.fieldBorder,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = colors.accent
        )
    )
}

// 分段选择器：选中项主色填充
@Composable
fun OreTabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.tabContainer)
            .border(1.dp, colors.panelBorder, RoundedCornerShape(8.dp))
    ) {
        tabs.forEachIndexed { index, tab ->
            val sel = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (sel) colors.accent else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    color = if (sel) colors.onAccent else colors.textSecondary,
                    fontFamily = PixelFontFamily,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// 区块标题："① 一句话画面"样式（主色序号 + 主文本标题）
@Composable
fun OreSectionTitle(
    index: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val marks = "①②③④⑤⑥⑦⑧⑨⑩"
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = marks.getOrNull(index)?.toString() ?: "·",
            color = colors.accent,
            fontFamily = PixelFontFamily,
            fontSize = 18.sp
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            color = colors.textPrimary,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
