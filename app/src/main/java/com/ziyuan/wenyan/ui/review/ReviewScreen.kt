package com.ziyuan.wenyan.ui.review

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OreButtonVariant
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreTopBar
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary

// 今日复习（艾宾浩斯遗忘曲线到期卡片）
@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    onOpenCard: (String) -> Unit
) {
    val viewModel: ReviewViewModel = hiltViewModel()
    val items by viewModel.items.collectAsStateWithLifecycle()

    OreBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            OreTopBar(
                title = "今日复习",
                modifier = Modifier.statusBarsPadding(),
                onBack = onBack
            )

            // 曲线说明
            Text(
                text = "按艾宾浩斯曲线：10分钟 → 1天 → 3天 → 7天 → 15天 → 30天",
                color = OreTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (items.isEmpty()) {
                // 空态
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    OrePanel(Modifier.padding(horizontal = 24.dp)) {
                        Text("今日无待复习卡片，去看看新字吧！", color = OreTextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.cardId }) { item ->
                        ReviewItemCard(
                            item = item,
                            onReviewed = { viewModel.markReviewed(item.cardId) },
                            onOpenCard = { onOpenCard(item.cardId) }
                        )
                    }
                }
            }
        }
    }
}

// 单条复习卡片
@Composable
private fun ReviewItemCard(
    item: ReviewItem,
    onReviewed: () -> Unit,
    onOpenCard: () -> Unit
) {
    OrePanel(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 字 / 标题
            Text(
                text = item.title,
                color = OreTextPrimary,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text("第 ${item.stage + 1} 次复习", color = OreTextPrimary, fontSize = 13.sp)
                Text("下次间隔 ${item.intervalText}", color = OreTextSecondary, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OreButton(
                text = "已复习",
                onClick = onReviewed,
                modifier = Modifier.weight(1f)
            )
            OreButton(
                text = "查看卡片",
                onClick = onOpenCard,
                modifier = Modifier.weight(1f),
                variant = OreButtonVariant.SECONDARY
            )
        }
    }
}
