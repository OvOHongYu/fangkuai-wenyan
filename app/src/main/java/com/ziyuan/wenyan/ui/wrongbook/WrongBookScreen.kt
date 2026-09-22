package com.ziyuan.wenyan.ui.wrongbook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OreChip
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreTopBar
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OreRed
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary

// 错题本
@Composable
fun WrongBookScreen(
    onBack: () -> Unit,
    onOpenCard: (String) -> Unit,
    onStartQuiz: () -> Unit
) {
    val viewModel: WrongBookViewModel = hiltViewModel()
    val items by viewModel.items.collectAsStateWithLifecycle()

    OreBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            OreTopBar(
                title = "错题本",
                modifier = Modifier.statusBarsPadding(),
                onBack = onBack
            )

            // 重练错题入口
            OreButton(
                text = "重练错题",
                onClick = onStartQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )

            if (items.isEmpty()) {
                // 空态
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    OrePanel(Modifier.padding(horizontal = 24.dp)) {
                        Text("错题本是空的，先去做几道题吧！", color = OreTextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        WrongItemCard(
                            item = item,
                            onDelete = { viewModel.delete(item.id) },
                            onOpenCard = onOpenCard
                        )
                    }
                }
            }
        }
    }
}

// 单条错题卡片
@Composable
private fun WrongItemCard(
    item: WrongBookItem,
    onDelete: () -> Unit,
    onOpenCard: (String) -> Unit
) {
    // 解析默认展开，可点击折叠
    var analysisExpanded by remember(item.id) { mutableStateOf(true) }

    OrePanel(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (item.practiced) 0.55f else 1f)
    ) {
        // 题目 + 删除
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = item.question,
                color = OreTextPrimary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "删除",
                color = OreTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable { onDelete() }
                    .padding(2.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("正确答案：${item.correctAnswer}", color = OreGreen, fontSize = 13.sp)
        Text("我的答案：${item.userAnswer}", color = OreRed, fontSize = 13.sp)

        // 解析（可折叠，默认展开）
        Row(
            modifier = Modifier
                .clickable { analysisExpanded = !analysisExpanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "解析 ${if (analysisExpanded) "▲" else "▼"}",
                color = OreTextSecondary,
                fontSize = 13.sp
            )
        }
        if (analysisExpanded && item.analysis.isNotBlank()) {
            Text(
                text = item.analysis,
                color = OreTextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
        Spacer(Modifier.height(6.dp))

        // 时间 / 来源 / 已重练 / 查看卡片
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.timeText, color = OreTextSecondary, fontSize = 11.sp)
                if (item.source.isNotBlank()) {
                    Text("来源：${item.source}", color = OreTextSecondary, fontSize = 11.sp)
                }
            }
            if (item.practiced) {
                OreChip(text = "已重练")
                Spacer(Modifier.width(6.dp))
            }
            item.cardId?.let { cardId ->
                OreChip(text = "查看卡片", onClick = { onOpenCard(cardId) })
            }
        }
    }
}
