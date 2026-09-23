package com.ziyuan.wenyan.ui.library

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.data.model.CardUnionFull
import com.ziyuan.wenyan.data.model.CompareCardFull
import com.ziyuan.wenyan.data.model.ShiCardFull
import com.ziyuan.wenyan.data.model.XuCardFull
import com.ziyuan.wenyan.ui.components.AlphabetIndexBar
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OreChip
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreTabRow
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.theme.PixelFontFamily
import com.ziyuan.wenyan.util.PinyinIndex
import kotlinx.coroutines.launch

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
                0 -> IndexedCardList(
                    entries = remember(shiCards) { shiCards.map { it.toShiEntry() } },
                    emptyHint = "暂无实词卡片",
                    onOpenCard = onOpenCard,
                    onOpenCompare = onOpenCompare
                )
                1 -> IndexedCardList(
                    entries = remember(xuCards) { xuCards.map { it.toXuEntry() } },
                    emptyHint = "暂无虚词卡片",
                    onOpenCard = onOpenCard,
                    onOpenCompare = onOpenCompare
                )
                2 -> IndexedCardList(
                    entries = remember(compareCards) { compareCards.map { it.toCompareEntry() } },
                    emptyHint = "暂无对比卡片",
                    onOpenCard = onOpenCard,
                    onOpenCompare = onOpenCompare
                )
                3 -> TextsTab(onOpenTexts)
                else -> IndexedCardList(
                    entries = remember(favorites) { favorites.map { it.toEntry() } },
                    emptyHint = "暂无收藏，去卡片页点亮 ★ 吧",
                    onOpenCard = onOpenCard,
                    onOpenCompare = onOpenCompare
                )
            }
        }
    }
}

// ===== 字母索引列表 =====

// 列表条目：字卡用 char / pinyin，对比卡用 title
private data class IndexEntry(
    val id: String,
    val letter: Char,      // 拼音首字母分组（无法识别时归 #）
    val sortKey: String,   // 组内排序键（去声调拼音）
    val char: String,
    val pinyin: String,
    val title: String,
    val learned: Boolean,
    val isCompare: Boolean
) {
    val label: String get() = char.ifEmpty { title }
}

private fun ShiCardFull.toShiEntry() = IndexEntry(
    id = id,
    letter = PinyinIndex.initialOf(card.pinyin, card.char),
    sortKey = PinyinIndex.sortKeyOf(card.pinyin, card.char),
    char = card.char, pinyin = card.pinyin, title = "",
    learned = learned, isCompare = false
)

private fun XuCardFull.toXuEntry() = IndexEntry(
    id = id,
    letter = PinyinIndex.initialOf(card.pinyin, card.char),
    sortKey = PinyinIndex.sortKeyOf(card.pinyin, card.char),
    char = card.char, pinyin = card.pinyin, title = "",
    learned = learned, isCompare = false
)

private fun CompareCardFull.toCompareEntry() = IndexEntry(
    id = id,
    letter = PinyinIndex.initialOf(card.pinyin, card.title),
    sortKey = PinyinIndex.sortKeyOf(card.pinyin, card.title),
    char = "", pinyin = "", title = card.title,
    learned = learned, isCompare = true
)

private fun CardUnionFull.toEntry(): IndexEntry = when (this) {
    is ShiCardFull -> toShiEntry()
    is XuCardFull -> toXuEntry()
    is CompareCardFull -> toCompareEntry()
}

// 排序：先按首字母（# 垫底），再按去声调拼音，最后按标题
private val ENTRY_ORDER = Comparator<IndexEntry> { a, b ->
    when {
        a.letter == b.letter -> {
            val byPinyin = a.sortKey.compareTo(b.sortKey)
            if (byPinyin != 0) byPinyin else a.label.compareTo(b.label)
        }
        a.letter == PinyinIndex.OTHER -> 1
        b.letter == PinyinIndex.OTHER -> -1
        else -> a.letter.compareTo(b.letter)
    }
}

// 带字母分组与侧边索引栏的卡片列表
@Composable
private fun IndexedCardList(
    entries: List<IndexEntry>,
    emptyHint: String,
    onOpenCard: (String) -> Unit,
    onOpenCompare: (String) -> Unit
) {
    if (entries.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            OrePanel(Modifier.fillMaxWidth()) {
                Text(
                    text = emptyHint,
                    color = OreTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        return
    }

    // 分组（每组：字母表头 + 若干卡片）
    val groups = remember(entries) { entries.sortedWith(ENTRY_ORDER).groupBy { it.letter } }
    // 每个字母在 LazyColumn 中的起始 item 下标
    val indexOfLetter = remember(groups) {
        val map = LinkedHashMap<Char, Int>()
        var cursor = 0
        groups.forEach { (letter, list) ->
            map[letter] = cursor
            cursor += list.size + 1
        }
        map
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var activeLetter by remember { mutableStateOf<Char?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 36.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            groups.forEach { (letter, list) ->
                item(key = "header_$letter") { LetterHeader(letter) }
                items(list, key = { it.id }) { entry ->
                    if (entry.isCompare) {
                        CompareRow(title = entry.title) { onOpenCompare(entry.id) }
                    } else {
                        CharRow(char = entry.char, pinyin = entry.pinyin, learned = entry.learned) {
                            onOpenCard(entry.id)
                        }
                    }
                }
            }
        }

        AlphabetIndexBar(
            letters = PinyinIndex.ALL_LETTERS,
            availableLetters = groups.keys,
            activeLetter = activeLetter,
            onLetterSelected = { letter, dragging ->
                activeLetter = letter
                indexOfLetter[letter]?.let { index ->
                    scope.launch {
                        // 点击用动画，拖动时直接跳转以免连续动画造成卡顿
                        if (dragging) listState.scrollToItem(index)
                        else listState.animateScrollToItem(index)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
        )
    }
}

// 字母分组表头
@Composable
private fun LetterHeader(letter: Char) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(colors.chipBg)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = letter.toString(),
                color = colors.accent,
                fontFamily = PixelFontFamily,
                fontSize = 13.sp
            )
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
