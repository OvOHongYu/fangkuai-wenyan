package com.ziyuan.wenyan.ui.card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ziyuan.wenyan.data.model.ChainStep
import com.ziyuan.wenyan.data.model.ShiCard
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OreButtonVariant
import com.ziyuan.wenyan.ui.components.OreSectionTitle
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 推导链动画弹窗：核心画面 → 逐条动画长出"条件 → 义项"分支（约 8 秒），支持跳过 / 重播
@Composable
fun ChainAnimationDialog(card: ShiCard, onDismiss: () -> Unit) {
    val steps = card.chain
    var replayKey by remember { mutableIntStateOf(0) }

    // 每条分支一个 Animatable(0f)，按时间轴逐条推进；重播时用 replayKey 重置
    val progressList = remember(replayKey) { List(steps.size) { Animatable(0f) } }
    val allDone by remember(replayKey) {
        derivedStateOf { progressList.all { it.value >= 1f } }
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(replayKey) {
        if (steps.isEmpty()) return@LaunchedEffect
        // 总时长约 8 秒：每条分支依次错峰出现
        val perBranch = (8000f / (steps.size + 1)).toInt().coerceIn(500, 1800)
        progressList.forEachIndexed { index, animatable ->
            launch {
                delay(index.toLong() * perBranch)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = perBranch, easing = LinearEasing)
                )
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.bgBottom)
                .systemBarsPadding()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OreSectionTitle(index = 2, title = "推导链动画")
                Spacer(Modifier.height(10.dp))

                // 顶部：核心画面（绿色边框强调）
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.colors.panel)
                        .border(2.dp, OreGreen, RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("核心画面", color = OreGreen, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = card.picture,
                        color = OreTextPrimary,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // 中部：主干 + 分支动画（可滚动）
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 6.dp)
                ) {
                    if (steps.isEmpty()) {
                        Text("该卡片暂无推导链", color = OreTextSecondary, fontSize = 14.sp)
                    } else {
                        SpineStub()
                        steps.forEachIndexed { index, step ->
                            BranchRow(step = step, progress = progressList[index])
                        }
                    }
                }

                // 底部按钮：播放中可跳过；播完显示"知道了"；随时可重播
                if (steps.isNotEmpty()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!allDone) {
                            OreButton(
                                text = "跳过",
                                onClick = { scope.launch { progressList.forEach { it.snapTo(1f) } } },
                                modifier = Modifier.weight(1f),
                                variant = OreButtonVariant.SECONDARY
                            )
                        }
                        OreButton(
                            text = "重播",
                            onClick = { replayKey++ },
                            modifier = Modifier.weight(1f),
                            variant = OreButtonVariant.SECONDARY
                        )
                        if (allDone) {
                            OreButton(
                                text = "知道了",
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 主干短竖线：从核心画面底部延伸到分支区
@Composable
private fun SpineStub() {
    // 在 composable 作用域取出主题色，供 Canvas 绘制使用
    val accent = OreGreen
    Canvas(
        Modifier
            .width(36.dp)
            .height(16.dp)
    ) {
        val spineX = size.width / 2f
        drawLine(
            color = accent,
            start = Offset(spineX, 0f),
            end = Offset(spineX, size.height),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// 单条分支行：左侧 Canvas 画随进度延伸的折线（竖干 + 横枝），右侧"条件 → 义项"文字渐显
@Composable
private fun BranchRow(step: ChainStep, progress: Animatable<Float, AnimationVector1D>) {
    val p = progress.value
    // 在 composable 作用域取出主题色，供 Canvas 绘制使用
    val accent = OreGreen
    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            Modifier
                .fillMaxHeight()
                .width(36.dp)
        ) {
            val spineX = size.width / 2f
            val midY = size.height / 2f
            // 前半段进度：竖干向下延伸；后半段进度：横枝向右延伸
            val vp = (p * 2f).coerceIn(0f, 1f)
            val hp = (p * 2f - 1f).coerceIn(0f, 1f)
            if (vp > 0f) {
                drawLine(
                    color = accent,
                    start = Offset(spineX, 0f),
                    end = Offset(spineX, size.height * vp),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            if (hp > 0f) {
                drawLine(
                    color = accent,
                    start = Offset(spineX, midY),
                    end = Offset(spineX + (size.width - spineX) * hp, midY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = accent,
                    radius = 3.dp.toPx(),
                    center = Offset(size.width, midY)
                )
            }
        }
        Column(
            Modifier
                .weight(1f)
                .alpha(alpha = p)
                .padding(start = 8.dp)
        ) {
            Text(text = step.condition, color = OreTextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
            Text(text = "→ ${step.to}", color = OreGreen, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}
