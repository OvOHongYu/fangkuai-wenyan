package com.ziyuan.wenyan.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OreButtonVariant
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreProgressBar
import com.ziyuan.wenyan.ui.components.OreSectionTitle
import com.ziyuan.wenyan.ui.theme.McFontFamily
import com.ziyuan.wenyan.ui.theme.OreGray
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary

// 我的（学习统计，底部 Tab 入口，无返回键）
@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
    onOpenWrongBook: () -> Unit
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OreBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // 标题区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "我的",
                    color = OreTextPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 学习统计（2×2）
                OreSectionTitle(index = 0, title = "学习统计")
                OrePanel(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth()) {
                        StatCell("打卡天数", "${state.checkInDays}", Modifier.weight(1f))
                        StatCell("已学卡片", "${state.learnedCards}", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth()) {
                        StatCell("平均正确率", state.accuracyText, Modifier.weight(1f))
                        StatCell("累计复习", "${state.totalReviews}", Modifier.weight(1f))
                    }
                }

                // 学习进度（实词 / 虚词）
                OreSectionTitle(index = 1, title = "学习进度")
                OrePanel(Modifier.fillMaxWidth()) {
                    ProgressRow("实词", state.shiLearned, state.shiTotal)
                    Spacer(Modifier.height(12.dp))
                    ProgressRow("虚词", state.xuLearned, state.xuTotal)
                }

                // 近 7 天学习（迷你柱状图）
                OreSectionTitle(index = 2, title = "近 7 天学习")
                OrePanel(Modifier.fillMaxWidth()) {
                    WeekChart(days = state.last7Days)
                }

                // 入口按钮
                OreButton(
                    text = "错题本",
                    onClick = onOpenWrongBook,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OreButtonVariant.SECONDARY
                )
                OreButton(
                    text = "设置",
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    variant = OreButtonVariant.SECONDARY
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// 统计格子：标签 + 大数字
@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = OreTextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = McFontFamily,
            fontSize = 24.sp,
            color = OreGreen
        )
    }
}

// 分类进度行：名称 + 进度条 + 已学/总数
@Composable
private fun ProgressRow(label: String, learned: Int, total: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = OreTextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.width(44.dp)
        )
        OreProgressBar(
            progress = if (total == 0) 0f else learned.toFloat() / total,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "$learned/$total",
            fontFamily = McFontFamily,
            fontSize = 13.sp,
            color = OreTextSecondary
        )
    }
}

// 近 7 天手绘迷你柱状图（今日绿色，其余灰）
@Composable
private fun WeekChart(days: List<DayBar>) {
    if (days.isEmpty()) return
    val max = days.maxOf { it.value }.coerceAtLeast(1)
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val barHeight = if (day.value <= 0) {
                        2.dp
                    } else {
                        (48.dp * (day.value.toFloat() / max)).coerceAtLeast(4.dp)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(barHeight)
                            .background(
                                if (day.isToday) OreGreen else OreGray,
                                RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                            )
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            days.forEach { day ->
                Text(
                    text = day.label,
                    color = if (day.isToday) OreGreen else OreTextSecondary,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
