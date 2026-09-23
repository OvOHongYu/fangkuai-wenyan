package com.ziyuan.wenyan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.PixelFontFamily
import kotlin.math.abs

// 侧边字母索引栏：按下即定位，继续拖动实时跟随；已存在的字母高亮，无卡片的字母淡显
@Composable
fun AlphabetIndexBar(
    letters: List<Char>,
    availableLetters: Set<Char>,
    activeLetter: Char?,
    onLetterSelected: (letter: Char, dragging: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 15.dp
) {
    if (letters.isEmpty()) return
    val colors = AppTheme.colors
    var pressed by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier) {
        // 高度不足时自动压缩行高，保证一屏内能放下全部字母
        val step = minOf(itemHeight, maxHeight / letters.size)

        Column(
            modifier = Modifier
                .width(24.dp)
                .pointerInput(letters, step, availableLetters) {
                    val px = step.toPx().coerceAtLeast(1f)

                    // 按纵坐标换算字母下标；只有真实存在的字母才回调定位
                    fun pick(y: Float, dragging: Boolean) {
                        val index = (y / px).toInt().coerceIn(0, letters.lastIndex)
                        val letter = letters[index]
                        if (letter in availableLetters) onLetterSelected(letter, dragging)
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown()
                        pressed = true
                        down.consume()
                        try {
                            pick(down.position.y, dragging = false)
                            val startY = down.position.y
                            var dragging = false
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    change.consume()
                                    break
                                }
                                if (!dragging && abs(change.position.y - startY) > 8f) dragging = true
                                pick(change.position.y, dragging)
                                change.consume()
                            }
                        } finally {
                            pressed = false
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            letters.forEach { letter ->
                val isActive = pressed && letter == activeLetter
                Box(
                    modifier = Modifier
                        .height(step)
                        .fillMaxWidth()
                        // 用带圆角的背景而非 clip，避免字体行高略大时字形被裁掉
                        .background(
                            color = if (isActive) colors.accent else Color.Transparent,
                            shape = RoundedCornerShape(3.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        color = when {
                            isActive -> colors.onAccent
                            letter in availableLetters -> colors.textSecondary
                            else -> colors.textSecondary.copy(alpha = 0.3f)
                        },
                        fontFamily = PixelFontFamily,
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        // 去掉字体额外行距，使字母在格子内垂直居中
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }
        }

        // 当前选中字母的大字提示，位于索引栏左侧
        if (pressed && activeLetter != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-38).dp)
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.panel)
                    .border(1.dp, colors.accent, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeLetter.toString(),
                    color = colors.accent,
                    fontFamily = PixelFontFamily,
                    fontSize = 24.sp
                )
            }
        }
    }
}
