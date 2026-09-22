package com.ziyuan.wenyan.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.ziyuan.wenyan.ui.components.OreChip
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreTabRow
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.theme.PixelFontFamily

// 卡片库：实词 / 虚词 / 对比 / 篇目库 / 收藏 五个分区
@Composable
fun LibraryScreen(
    onOpenCard: (String) -> Unit,
    onOpenCompare: (String) -> Unit,
    onOpenTexts: () -> Unit
) {
    val vm: LibraryViewModel = hiltViewModel()
    val shiCards by vm.shiCards.collectAsStateWithLifecycle()
    val xuCards by vm.xuCards.collectAsStateWithLifecycle()
    val compareCards by vm.compareCards.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()

    // 当前选中分区
    var tab by rememberSaveable { mutableStateOf(0) }

    OreBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "卡片库",
                color = OreTextPrimary,
                fontFamily = PixelFontFamily,
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
            OreTabRow(
                tabs = listOf("实词", "虚词", "对比", "篇目库", "收藏"),
                selected = tab,
                onSelect = { tab = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
            when (tab) {
                0 -> ShiTab(shiCards, onOpenCard)
                1 -> XuTab(xuCards, onOpenCard)
                2 -> CompareTab(compareCards, onOpenCompare)
                3 -> TextsTab(onOpenTexts)
                else -> FavoriteTab(favorites, onOpenCard, onOpenCompare)
            }
        }
    }
}

// 实词分区：按拼音 sortKey 排序的列表
@Composable
private fun ShiTab(cards: List<ShiCardFull>, onOpenCard: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(cards, key = { it.id }) { item ->
            CharRow(char = item.card.char, pinyin = item.card.pinyin, learned = item.learned) {
                onOpenCard(item.id)
            }
        }
    }
}

// 虚词分区：按功能分类分组展示
@Composable
private fun XuTab(cards: List<XuCardFull>, onOpenCard: (String) -> Unit) {
    val groups = cards.groupBy { it.card.category.ifBlank { "其他" } }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        groups.forEach { (category, list) ->
            item(key = "header_$category") {
                OreChip(text = category, selected = true)
            }
            items(list, key = { it.id }) { item ->
                CharRow(char = item.card.char, pinyin = item.card.pinyin, learned = item.learned) {
                    onOpenCard(item.id)
                }
            }
        }
    }
}

// 对比分区：对比卡标题列表
@Composable
private fun CompareTab(cards: List<CompareCardFull>, onOpenCompare: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(cards, key = { it.id }) { item ->
            CompareRow(title = item.card.title) { onOpenCompare(item.id) }
        }
    }
}

// 篇目库分区：说明 + 入口按钮
@Composable
private fun TextsTab(onOpenTexts: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        OrePanel(Modifier.fillMaxWidth()) {
            Text(
                text = "统编版高中文言文与古诗词全篇目，含翻译与字词注释",
                color = OreTextSecondary,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            OreButton(
                text = "打开篇目库",
                onClick = { onOpenTexts() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 收藏分区：实词/虚词 显示 字+拼音，对比 显示 title
@Composable
private fun FavoriteTab(
    favorites: List<CardUnionFull>,
    onOpenCard: (String) -> Unit,
    onOpenCompare: (String) -> Unit
) {
    if (favorites.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            OrePanel(Modifier.fillMaxWidth()) {
                Text(
                    text = "暂无收藏，去卡片页点亮 ★ 吧",
                    color = OreTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(favorites, key = { it.id }) { fav ->
            when (fav) {
                is ShiCardFull -> CharRow(char = fav.card.char, pinyin = fav.card.pinyin, learned = fav.learned) {
                    onOpenCard(fav.id)
                }
                is XuCardFull -> CharRow(char = fav.card.char, pinyin = fav.card.pinyin, learned = fav.learned) {
                    onOpenCard(fav.id)
                }
                is CompareCardFull -> CompareRow(title = fav.card.title) { onOpenCompare(fav.id) }
            }
        }
    }
}

// 字卡行：大字 + 拼音 + 已学标记
@Composable
private fun CharRow(char: String, pinyin: String, learned: Boolean, onClick: () -> Unit) {
    OrePanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = char,
                color = OreTextPrimary,
                fontFamily = PixelFontFamily,
                fontSize = 32.sp
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = pinyin,
                color = OreTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (learned) {
                OreChip(text = "已学", selected = true)
            }
        }
    }
}

// 对比卡行：标题 + 类型标签
@Composable
private fun CompareRow(title: String, onClick: () -> Unit) {
    OrePanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = OreTextPrimary,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            OreChip(text = "对比")
        }
    }
}
