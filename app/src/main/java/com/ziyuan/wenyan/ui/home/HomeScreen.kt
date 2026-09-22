package com.ziyuan.wenyan.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.data.model.CardUnionFull
import com.ziyuan.wenyan.data.model.CompareCardFull
import com.ziyuan.wenyan.data.model.ShiCardFull
import com.ziyuan.wenyan.data.model.XuCardFull
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OreButtonVariant
import com.ziyuan.wenyan.ui.components.OreChip
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreProgressBar
import com.ziyuan.wenyan.ui.components.OreSectionTitle
import com.ziyuan.wenyan.ui.components.OreTextField
import com.ziyuan.wenyan.ui.theme.McFontFamily
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.theme.PixelFontFamily

// 首页：搜索 / 今日复习 / 学习进度 / 快捷入口 / 随机一字
@Composable
fun HomeScreen(
    onOpenCard: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenTexts: () -> Unit,
    onStartReview: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val vm: HomeViewModel = hiltViewModel()
    val dueCount by vm.dueCount.collectAsStateWithLifecycle()
    val shiCards by vm.shiCards.collectAsStateWithLifecycle()
    val xuCards by vm.xuCards.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val searchResults by vm.searchResults.collectAsStateWithLifecycle()
    val randomCard by vm.randomCard.collectAsStateWithLifecycle()

    OreBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            // 页面标题（像素字体）
            Text(
                text = "字源 · 文言实词",
                color = OreTextPrimary,
                fontFamily = PixelFontFamily,
                fontSize = 24.sp
            )
            Spacer(Modifier.height(16.dp))

            // 搜索区
            SearchSection(onSearch = { query -> vm.search(query, onOpenCard) })

            // 搜索结果 / 未找到提示（恰好 1 条时由 VM 直接打开卡片，不展示）
            if (searchQuery.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                SearchResultSection(searchQuery, searchResults, onOpenCard)
            }
            Spacer(Modifier.height(10.dp))

            // 今日复习
            ReviewSection(dueCount, onStartReview)
            Spacer(Modifier.height(10.dp))

            // 学习进度
            ProgressSection(shiCards, xuCards)
            Spacer(Modifier.height(10.dp))

            // 快捷入口
            QuickEntrySection(onOpenLibrary, onOpenTexts)
            Spacer(Modifier.height(10.dp))

            // 随机一字
            RandomSection(randomCard) { vm.randomOne() }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// 搜索区：输入框 + 搜索按钮
@Composable
private fun SearchSection(onSearch: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    OrePanel(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OreTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = "搜索汉字…  如：度 / ài"
            )
            OreButton(
                text = "搜索",
                onClick = { onSearch(query) },
                enabled = query.isNotBlank()
            )
        }
    }
}

// 搜索结果区：多条列出，0 条显示未找到提示
@Composable
private fun SearchResultSection(
    query: String,
    results: List<CardUnionFull>,
    onOpenCard: (String) -> Unit
) {
    if (results.isEmpty()) {
        OrePanel(Modifier.fillMaxWidth()) {
            Text(
                text = "未找到「$query」相关卡片",
                color = OreTextSecondary,
                fontSize = 14.sp
            )
        }
    } else if (results.size > 1) {
        OrePanel(Modifier.fillMaxWidth()) {
            Text(
                text = "找到 ${results.size} 张相关卡片",
                color = OreTextSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            results.forEach { result ->
                SearchResultRow(result) { onOpenCard(result.id) }
            }
        }
    }
}

// 搜索结果行：字 + 拼音 + 类型标签
@Composable
private fun SearchResultRow(result: CardUnionFull, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (result) {
            is ShiCardFull -> {
                Text(
                    text = result.card.char,
                    color = OreTextPrimary,
                    fontFamily = PixelFontFamily,
                    fontSize = 20.sp
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = result.card.pinyin,
                    color = OreTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                OreChip(text = "实词")
            }
            is XuCardFull -> {
                Text(
                    text = result.card.char,
                    color = OreTextPrimary,
                    fontFamily = PixelFontFamily,
                    fontSize = 20.sp
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = result.card.pinyin,
                    color = OreTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                OreChip(text = "虚词")
            }
            is CompareCardFull -> {
                Text(
                    text = result.card.title,
                    color = OreTextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                OreChip(text = "对比")
            }
        }
    }
}

// 今日复习：进度条 + 待复习数 + 开始按钮
@Composable
private fun ReviewSection(dueCount: Int, onStartReview: () -> Unit) {
    OrePanel(Modifier.fillMaxWidth()) {
        OreSectionTitle(index = 0, title = "今日复习")
        Spacer(Modifier.height(10.dp))
        OreProgressBar(progress = dueCount / 10f)
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (dueCount == 0) "今日无待复习，真棒！" else "待复习：$dueCount 张",
            color = if (dueCount == 0) OreTextSecondary else OreTextPrimary,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(12.dp))
        OreButton(
            text = "开始复习",
            onClick = { onStartReview() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// 学习进度：实词 / 虚词 两列已学统计
@Composable
private fun ProgressSection(shiCards: List<ShiCardFull>, xuCards: List<XuCardFull>) {
    OrePanel(Modifier.fillMaxWidth()) {
        OreSectionTitle(index = 1, title = "学习进度")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProgressColumn("实词", shiCards.count { it.learned }, shiCards.size, Modifier.weight(1f))
            ProgressColumn("虚词", xuCards.count { it.learned }, xuCards.size, Modifier.weight(1f))
        }
    }
}

// 单列进度：已学/总数 + 进度条 + 百分比（数字用 MC 字体）
@Composable
private fun ProgressColumn(label: String, learned: Int, total: Int, modifier: Modifier = Modifier) {
    val percent = if (total > 0) learned * 100 / total else 0
    Column(modifier) {
        Text(text = label, color = OreTextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$learned/$total",
            color = OreTextPrimary,
            fontFamily = McFontFamily,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(6.dp))
        OreProgressBar(progress = if (total > 0) learned.toFloat() / total else 0f)
        Spacer(Modifier.height(6.dp))
        Text(text = "$percent%", color = OreGreen, fontFamily = McFontFamily, fontSize = 13.sp)
    }
}

// 快捷入口：三个分类 Chip + 篇目库按钮
@Composable
private fun QuickEntrySection(onOpenLibrary: () -> Unit, onOpenTexts: () -> Unit) {
    OrePanel(Modifier.fillMaxWidth()) {
        OreSectionTitle(index = 2, title = "快捷入口")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OreChip(
                text = "实词卡片",
                modifier = Modifier.weight(1f),
                onClick = onOpenLibrary
            )
            OreChip(
                text = "虚词卡片",
                modifier = Modifier.weight(1f),
                onClick = onOpenLibrary
            )
            OreChip(
                text = "易混对比",
                modifier = Modifier.weight(1f),
                onClick = onOpenLibrary
            )
        }
        Spacer(Modifier.height(8.dp))
        OreButton(
            text = "📖 篇目库",
            onClick = { onOpenTexts() },
            modifier = Modifier.fillMaxWidth(),
            variant = OreButtonVariant.SECONDARY
        )
    }
}

// 随机一字：抽中后展示 字 + 拼音 + 一句话画面，可重抽
@Composable
private fun RandomSection(randomCard: ShiCardFull?, onDraw: () -> Unit) {
    OrePanel(Modifier.fillMaxWidth()) {
        OreSectionTitle(index = 3, title = "随机一字")
        Spacer(Modifier.height(10.dp))
        val card = randomCard?.card
        if (card != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = card.char,
                    color = OreTextPrimary,
                    fontFamily = PixelFontFamily,
                    fontSize = 40.sp
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(text = card.pinyin, color = OreTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(text = card.picture, color = OreTextPrimary, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        OreButton(
            text = "🎲 随机一字",
            onClick = { onDraw() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
