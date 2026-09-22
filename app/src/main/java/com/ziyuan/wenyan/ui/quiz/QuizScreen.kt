package com.ziyuan.wenyan.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OreChip
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreProgressBar
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.McFontFamily
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OreRed
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.theme.OreYellow
import com.ziyuan.wenyan.util.QuizQuestion
import kotlin.math.roundToInt

// 检测页（底部 Tab 入口，无返回键，仅标题区）
@Composable
fun QuizScreen(
    onOpenCard: (String) -> Unit,
    mode: String = "daily",
    sourceId: String? = null
) {
    val viewModel: QuizViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OreBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 标题区 + 模式标签
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "检测",
                    color = OreTextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                OreChip(text = quizModeLabel(viewModel.mode), selected = true)
            }

            // 页头提示（如：该卡片题目不足，已用全部卡片出题）
            state.notice?.let { notice ->
                Text(
                    text = notice,
                    color = OreYellow,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            when {
                state.loading -> Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("正在出题…", color = OreTextSecondary)
                }
                state.finished -> QuizResultView(
                    state = state,
                    onRestart = viewModel::restart,
                    modifier = Modifier.weight(1f)
                )
                state.empty -> Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    OrePanel(Modifier.padding(horizontal = 24.dp)) {
                        Text("暂无可出的题目，先去学习一些卡片吧！", color = OreTextSecondary)
                    }
                }
                else -> QuizContentView(
                    state = state,
                    onSelect = viewModel::select,
                    onSubmit = viewModel::submit,
                    onNext = viewModel::next,
                    onOpenCard = onOpenCard,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 模式中文名
private fun quizModeLabel(mode: String): String = when (mode) {
    "compare" -> "对比检测"
    "card" -> "卡片检测"
    "review" -> "复习检测"
    "wrong" -> "错题重练"
    else -> "每日检测"
}

// 答题视图：进度条 + 题干 + 选项 + 提交/反馈
@Composable
private fun QuizContentView(
    state: QuizUiState,
    onSelect: (Int) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onOpenCard: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val question = state.questions[state.currentIndex]
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "第 ${state.currentIndex + 1} / ${state.total} 题",
            color = OreTextSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(6.dp))
        OreProgressBar(
            progress = (state.currentIndex + 1f) / state.total.coerceAtLeast(1)
        )
        Spacer(Modifier.height(12.dp))

        // 题干（句子单独一行醒目）
        OrePanel(Modifier.fillMaxWidth()) {
            Text(
                text = question.question,
                color = OreTextPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            if (question.sentence.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = question.sentence, color = OreGreen, fontSize = 16.sp, lineHeight = 23.sp)
            }
        }
        Spacer(Modifier.height(12.dp))

        // 选项列表：矮卡面板，选中绿框绿字 + 深绿底
        question.options.forEachIndexed { index, option ->
            val selected = state.selectedIndex == index
            val isCorrect = state.submitted && index == question.correctIndex
            val isWrongPick = state.submitted && selected && !isCorrect
            val borderColor = when {
                isCorrect -> OreGreen
                isWrongPick -> OreRed
                selected -> OreGreen
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) AppTheme.colors.accent.copy(alpha = 0.18f) else Color.Transparent)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable(enabled = !state.submitted) { onSelect(index) }
            ) {
                OrePanel(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${"ABCDEF".getOrNull(index) ?: "·"}.",
                            color = if (selected || isCorrect) OreGreen else OreTextSecondary,
                            fontFamily = McFontFamily,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = option,
                            color = when {
                                isWrongPick -> OreRed
                                selected || isCorrect -> OreGreen
                                else -> OreTextPrimary
                            },
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        if (!state.submitted) {
            // 提交按钮（未选禁用）
            OreButton(
                text = "提交答案",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedIndex != null
            )
        } else {
            // 提交反馈
            val correct = state.selectedIndex == question.correctIndex
            OrePanel(Modifier.fillMaxWidth()) {
                if (correct) {
                    Text("✅ 回答正确！", color = OreGreen, fontSize = 15.sp)
                } else {
                    Text(
                        text = "❌ 回答错误，正确答案：${question.options.getOrNull(question.correctIndex) ?: ""}",
                        color = OreRed,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
                if (question.analysis.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = question.analysis,
                        color = OreTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
                if (question.source.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("来源：${question.source}", color = OreTextSecondary, fontSize = 12.sp)
                }
                question.cardId?.let { cardId ->
                    Spacer(Modifier.height(8.dp))
                    OreChip(text = "查看卡片 ▸", onClick = { onOpenCard(cardId) })
                }
                Spacer(Modifier.height(10.dp))
                OreButton(
                    text = if (state.currentIndex + 1 >= state.total) "完成" else "下一题",
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// 完成页：正确率大数字 + 进度条 + 统计 + 再来一组
@Composable
private fun QuizResultView(
    state: QuizUiState,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rate = if (state.total == 0) 0f else state.correctCount.toFloat() / state.total
    val percent = (rate * 100).roundToInt()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        OrePanel(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("检测完成", color = OreTextPrimary, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "$percent%",
                    fontFamily = McFontFamily,
                    fontSize = 48.sp,
                    color = OreGreen
                )
                Spacer(Modifier.height(12.dp))
                OreProgressBar(progress = rate)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "共 ${state.total} 题 · 答对 ${state.correctCount} 题",
                    color = OreTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        OreButton(text = "再来一组", onClick = onRestart, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
    }
}
