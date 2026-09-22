package com.ziyuan.wenyan.ui.texts

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.data.model.Annotation
import com.ziyuan.wenyan.data.model.TextPiece
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreChip
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreTabRow
import com.ziyuan.wenyan.ui.components.OreTopBar
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.theme.PixelFontFamily

// 篇目详情：头部信息 + 原文 / 译文 / 注释 三视图
@Composable
fun TextDetailScreen(
    textId: String,
    onBack: () -> Unit
) {
    val viewModel: TextDetailViewModel = hiltViewModel()
    // 绑定篇目 id，触发数据加载
    LaunchedEffect(textId) { viewModel.bind(textId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    // 当前选中 Tab：0 原文 / 1 译文 / 2 注释
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    OreBackground {
        Column(Modifier.fillMaxSize()) {
            OreTopBar(title = "篇目详情", onBack = onBack, modifier = Modifier.statusBarsPadding())
            when (val s = state) {
                TextDetailUiState.Loading -> StateHint("加载中…", Modifier.fillMaxWidth().weight(1f))
                TextDetailUiState.NotFound -> StateHint("未找到该篇目", Modifier.fillMaxWidth().weight(1f))
                is TextDetailUiState.Ready -> DetailContent(
                    piece = s.piece,
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
    }
}

// 居中提示（加载中 / 未找到）
@Composable
private fun StateHint(text: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = OreTextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// 详情主体：头部面板 + Tab 行 + 当前 Tab 内容
@Composable
private fun DetailContent(
    piece: TextPiece,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 头部信息面板
        item(key = "header") { HeaderPanel(piece) }
        // 三视图切换
        item(key = "tabs") {
            OreTabRow(
                tabs = listOf("原文", "译文", "注释"),
                selected = selectedTab,
                onSelect = onSelectTab
            )
        }
        // 当前 Tab 内容
        if (selectedTab == 0) {
            item(key = "content") { OriginalTextBlock(piece) }
        } else if (selectedTab == 1) {
            item(key = "content") { TranslationBlock(piece) }
        } else {
            if (piece.annotations.isEmpty()) {
                item(key = "empty_annotations") {
                    Text(
                        text = "暂无注释",
                        color = OreTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                itemsIndexed(piece.annotations, key = { index, _ -> "ann_$index" }) { _, ann ->
                    AnnotationCard(ann)
                }
            }
        }
    }
}

// 头部面板：标题（像素字体 22sp）+ 作者·朝代 + 册别 / 体裁 Chip
@Composable
private fun HeaderPanel(piece: TextPiece) {
    OrePanel(Modifier.fillMaxWidth()) {
        Text(
            text = piece.title,
            color = OreTextPrimary,
            fontFamily = PixelFontFamily,
            fontSize = 22.sp,
            lineHeight = 32.sp
        )
        val meta = listOf(piece.author, piece.dynasty)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        if (meta.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = meta,
                color = OreTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (piece.book.isNotBlank()) {
                OreChip(text = piece.book)
            }
            if (piece.genre.isNotBlank()) {
                OreChip(text = piece.genre)
            }
        }
    }
}

// 原文：普通阅读模式，纯文本展示，不做任何字词标记
// （仅从卡片"出现篇目"进入的定位阅读窗口才有双色高亮）
@Composable
private fun OriginalTextBlock(piece: TextPiece) {
    Text(
        text = piece.content,
        color = OreTextPrimary,
        style = MaterialTheme.typography.bodyLarge
    )
}

// 译文全文
@Composable
private fun TranslationBlock(piece: TextPiece) {
    if (piece.translation.isBlank()) {
        Text(
            text = "暂无译文",
            color = OreTextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    } else {
        Text(
            text = piece.translation,
            color = OreTextSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// 注释条目矮卡：字词（绿色像素字）+ 释义 + 备注（空则不显示）
@Composable
private fun AnnotationCard(annotation: Annotation) {
    OrePanel(Modifier.fillMaxWidth()) {
        Text(
            text = annotation.word,
            color = OreGreen,
            fontFamily = PixelFontFamily,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = annotation.meaning,
            color = OreTextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
        if (annotation.note.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = annotation.note,
                color = OreTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
