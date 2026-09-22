package com.ziyuan.wenyan.ui.texts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ziyuan.wenyan.data.model.TextPiece
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OreButtonVariant
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreSectionTitle
import com.ziyuan.wenyan.ui.components.OreTextField
import com.ziyuan.wenyan.ui.components.OreTopBar
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.theme.OreYellow

// 篇目库：分组浏览 + 标题/作者搜索
@Composable
fun TextLibraryScreen(
    onBack: () -> Unit,
    onOpenText: (String) -> Unit
) {
    val viewModel: TextLibraryViewModel = hiltViewModel()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()

    OreBackground {
        Column(Modifier.fillMaxSize()) {
            // 顶栏
            OreTopBar(title = "篇目库", onBack = onBack, modifier = Modifier.statusBarsPadding())

            // 搜索行：输入框 + 搜索按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OreTextField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = "搜索篇目标题 / 作者…"
                )
                Spacer(Modifier.width(10.dp))
                OreButton(
                    text = "搜索",
                    onClick = { viewModel.search() },
                    variant = OreButtonVariant.SECONDARY
                )
            }

            // 内容区：搜索结果态 / 分组浏览态
            val results = searchResults
            val groupList = groups
            Box(Modifier.fillMaxWidth().weight(1f)) {
                when {
                    // 搜索结果态
                    results != null -> {
                        if (results.isEmpty()) EmptyHint() else SearchResultList(results, onOpenText)
                    }
                    // 数据尚未加载完成，先留白
                    groupList == null -> Unit
                    // 库为空
                    groupList.isEmpty() -> EmptyHint()
                    // 分组浏览态
                    else -> GroupedTextList(groupList, onOpenText)
                }
            }
        }
    }
}

// 搜索结果列表（平铺，不分组）
@Composable
private fun SearchResultList(results: List<TextPiece>, onOpenText: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(results, key = { it.id }) { piece ->
            TextPieceItem(piece = piece, onClick = { onOpenText(piece.id) })
        }
    }
}

// 分组浏览列表：每组头为书名 + 数量，组内篇目条目
@Composable
private fun GroupedTextList(groups: List<TextGroup>, onOpenText: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        groups.forEachIndexed { index, group ->
            item(key = "header_${group.book}") {
                OreSectionTitle(index = index, title = "${group.book} · ${group.pieces.size} 篇")
            }
            items(group.pieces, key = { it.id }) { piece ->
                TextPieceItem(piece = piece, onClick = { onOpenText(piece.id) })
            }
        }
    }
}

// 篇目条目：标题 + 作者·朝代 + 体裁标签
@Composable
private fun TextPieceItem(piece: TextPiece, onClick: () -> Unit) {
    OrePanel(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = piece.title,
                    color = OreTextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(2.dp))
                val meta = listOf(piece.author, piece.dynasty)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                    .ifBlank { "—" }
                Text(
                    text = meta,
                    color = OreTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.width(10.dp))
            if (piece.genre.isNotBlank()) {
                GenreChip(genre = piece.genre)
            }
        }
    }
}

// 体裁标签：文言文绿边 / 诗词黄边（样式与 OreChip 一致，仅边框与文字色区分）
@Composable
private fun GenreChip(genre: String) {
    val accent = if (genre == "诗词") OreYellow else OreGreen
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AppTheme.colors.chipBg)
            .border(1.dp, accent, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = genre, color = accent, fontSize = 13.sp)
    }
}

// 空态：库为空或搜索无结果
@Composable
private fun EmptyHint() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "未找到相关篇目",
            color = OreTextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
