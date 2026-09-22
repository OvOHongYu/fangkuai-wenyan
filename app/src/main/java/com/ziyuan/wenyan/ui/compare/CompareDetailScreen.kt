package com.ziyuan.wenyan.ui.compare

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.data.model.CompareTableRow
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreSectionTitle
import com.ziyuan.wenyan.ui.components.OreTopBar
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OrePanelBorder
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.theme.PixelFontFamily

// 对比卡片页：头部 vs 大字 + 对比表 + 例句对比 + 一句话区分 + 底部对比检测
@Composable
fun CompareDetailScreen(
    compareId: String,
    onBack: () -> Unit,
    onStartCompareQuiz: (String) -> Unit
) {
    val viewModel: CompareDetailViewModel = hiltViewModel()
    val card by viewModel.card.collectAsStateWithLifecycle()

    LaunchedEffect(compareId) { viewModel.load(compareId) }

    OreBackground {
        Column(Modifier.fillMaxSize()) {
            OreTopBar(
                title = card?.card?.title?.takeIf { it.isNotBlank() } ?: "对比详情",
                modifier = Modifier.statusBarsPadding(),
                onBack = onBack
            )

            // 内容区（可滚动）
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                val current = card
                if (current == null) {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("加载中…", color = OreTextSecondary, fontSize = 14.sp)
                    }
                } else {
                    // 头部：左右字 vs + 核心差异
                    OrePanel(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (current.card.charLeft.isNotBlank() && current.card.charRight.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = current.card.charLeft,
                                        color = OreTextPrimary,
                                        fontFamily = PixelFontFamily,
                                        fontSize = 40.sp
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text("vs", color = OreTextSecondary, fontSize = 16.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = current.card.charRight,
                                        color = OreTextPrimary,
                                        fontFamily = PixelFontFamily,
                                        fontSize = 40.sp
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            Text(
                                text = current.card.coreDifference,
                                color = OreTextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))

                    // 用法对比表
                    OreSectionTitle(index = 0, title = "用法对比")
                    Spacer(Modifier.height(8.dp))
                    OrePanel(Modifier.fillMaxWidth()) {
                        // 表头
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("用法", color = OreTextSecondary, fontSize = 12.sp, modifier = Modifier.width(64.dp))
                            Text(
                                text = current.card.charLeft,
                                color = OreGreen,
                                fontFamily = PixelFontFamily,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = current.card.charRight,
                                color = OreGreen,
                                fontFamily = PixelFontFamily,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(OrePanelBorder)
                        )
                        Spacer(Modifier.height(10.dp))
                        if (current.card.table.isEmpty()) {
                            Text("暂无对比数据", color = OreTextSecondary, fontSize = 13.sp)
                        }
                        current.card.table.forEach { row ->
                            CompareRow(row = row)
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                    Spacer(Modifier.height(14.dp))

                    // 例句对比区：左右 ✅ 例句分别列出
                    OreSectionTitle(index = 1, title = "例句对比")
                    Spacer(Modifier.height(8.dp))
                    OrePanel(Modifier.fillMaxWidth()) {
                        val leftExamples = current.card.table.mapNotNull { row ->
                            if (row.left.startsWith("✅")) current.card.charLeft to row.left.removePrefix("✅").trim() else null
                        }
                        val rightExamples = current.card.table.mapNotNull { row ->
                            if (row.right.startsWith("✅")) current.card.charRight to row.right.removePrefix("✅").trim() else null
                        }
                        if (leftExamples.isEmpty() && rightExamples.isEmpty()) {
                            Text("暂无例句", color = OreTextSecondary, fontSize = 13.sp)
                        } else {
                            if (leftExamples.isNotEmpty()) {
                                Text(current.card.charLeft, color = OreGreen, fontFamily = PixelFontFamily, fontSize = 15.sp)
                                Spacer(Modifier.height(6.dp))
                                leftExamples.forEach { (_, sentence) ->
                                    Text("· $sentence", color = OreTextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
                                    Spacer(Modifier.height(4.dp))
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                            if (rightExamples.isNotEmpty()) {
                                Text(current.card.charRight, color = OreGreen, fontFamily = PixelFontFamily, fontSize = 15.sp)
                                Spacer(Modifier.height(6.dp))
                                rightExamples.forEach { (_, sentence) ->
                                    Text("· $sentence", color = OreTextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))

                    // 一句话区分
                    if (current.card.oneLine.isNotBlank()) {
                        OreSectionTitle(index = 2, title = "一句话区分")
                        Spacer(Modifier.height(8.dp))
                        OrePanel(Modifier.fillMaxWidth()) {
                            Text(
                                text = current.card.oneLine,
                                color = OreGreen,
                                fontFamily = PixelFontFamily,
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // 底部固定按钮
            Column(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OrePanelBorder)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.topBar)
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    OreButton(
                        text = "开始对比检测",
                        onClick = { onStartCompareQuiz(compareId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// 对比表单行：用法名 + 左右单元格（✅ 绿色例句 / ❌ 灰色"不支持"）
@Composable
private fun CompareRow(row: CompareTableRow) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = row.relation,
            color = OreTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            modifier = Modifier.width(64.dp)
        )
        CompareCell(text = row.left, modifier = Modifier.weight(1f))
        CompareCell(text = row.right, modifier = Modifier.weight(1f))
    }
}

// 单元格：按前缀符号解析显示
@Composable
private fun CompareCell(text: String, modifier: Modifier = Modifier) {
    val isYes = text.startsWith("✅")
    val isNo = text.startsWith("❌")
    val shown = when {
        isNo -> "不支持"
        isYes -> text.removePrefix("✅").trim()
        else -> text
    }
    Text(
        text = shown,
        color = when {
            isYes -> OreGreen
            isNo -> OreTextSecondary
            else -> OreTextPrimary
        },
        fontSize = 13.sp,
        lineHeight = 19.sp,
        modifier = modifier
    )
}
