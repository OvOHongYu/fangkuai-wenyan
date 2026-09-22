package com.ziyuan.wenyan.ui.texts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.model.Occurrence
import com.ziyuan.wenyan.data.model.TextPiece
import com.ziyuan.wenyan.data.repository.TextRepository
import com.ziyuan.wenyan.ui.components.OreChip
import com.ziyuan.wenyan.ui.components.OreTabRow
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.PixelFontFamily
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 定位阅读窗口 ViewModel：按 textId 加载篇目
@HiltViewModel
class TextReadingViewModel @Inject constructor(
    private val textRepository: TextRepository
) : ViewModel() {

    private val textIdFlow = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val text: StateFlow<TextPiece?> = textIdFlow
        .filterNotNull()
        .flatMapLatest { textRepository.observeText(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun load(textId: String) {
        textIdFlow.value = textId
    }
}

// 点击标记词后弹出的释义信息
private data class TapInfo(val word: String, val meaning: String, val note: String, val isSame: Boolean)

// 3/4 屏底部定位阅读窗口：只高亮当前卡片的字，同义 / 异义双色，点击标记词看释义
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextReadingSheet(
    textId: String,        // 篇目 id
    highlightChar: String, // 要高亮的字（如"爱"）
    meaning: String,       // 当前义项名（如"吝惜"）
    onDismiss: () -> Unit
) {
    val viewModel: TextReadingViewModel = hiltViewModel()
    LaunchedEffect(textId) { viewModel.load(textId) }
    val text by viewModel.text.collectAsStateWithLifecycle()
    val colors = AppTheme.colors

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var tab by remember { mutableIntStateOf(0) }
    var tapped by remember { mutableStateOf<TapInfo?>(null) }
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (colors.isDark) Color(0xFF222222) else colors.panel,
        tonalElevation = 0.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding()
        ) {
            // 头部：篇目信息 + 关闭按钮
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = text?.title ?: "加载中…",
                        color = colors.textPrimary,
                        fontFamily = PixelFontFamily,
                        fontSize = 17.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val piece = text
                        val meta = piece?.let {
                            listOf(it.author, it.dynasty).filter { part -> part.isNotBlank() }
                                .joinToString("·")
                        }.orEmpty()
                        if (meta.isNotBlank()) {
                            Text(meta, color = colors.textSecondary, fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                        }
                        if (!piece?.book.isNullOrBlank()) {
                            OreChip(text = piece!!.book)
                        }
                    }
                }
                Text(
                    text = "✕",
                    color = colors.textSecondary,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(8.dp)
                )
            }

            // 图例 + 操作提示
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendBox(color = colors.sameHighlight, label = "「$meaning」")
                LegendBox(color = colors.diffHighlight, label = "「$highlightChar」其他义项")
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "点击带色标记的字可查看该处释义",
                color = colors.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))

            // 原文 / 译文 / 注释 切换
            OreTabRow(
                tabs = listOf("原文", "译文", "注释"),
                selected = tab,
                onSelect = { tab = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))

            // 内容区
            val piece = text
            if (piece == null) {
                Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("加载中…", color = colors.textSecondary, fontSize = 14.sp)
                }
            } else {
                // 只取当前卡片的字的出现位置：同义 / 异义都来自同一个字
                val hits = remember(piece, highlightChar) {
                    piece.occurrences.filter { it.char == highlightChar }.sortedBy { it.start }
                }
                when (tab) {
                    // 原文：双色高亮 + 自动定位 + 点击查看释义
                    0 -> {
                        LaunchedEffect(piece.id, piece.content, tab) {
                            val target = hits.firstOrNull()?.start
                                ?: piece.content.indexOf(highlightChar)
                            if (target >= 0 && piece.content.isNotEmpty()) {
                                delay(150) // 等待布局完成后再定位
                                val ratio = target.toFloat() / piece.content.length
                                scrollState.animateScrollTo((ratio * scrollState.maxValue).toInt())
                            }
                        }
                        var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                        Text(
                            text = remember(piece, highlightChar, meaning, colors) {
                                buildHighlighted(piece, highlightChar, meaning, hits, colors)
                            },
                            color = colors.textPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            onTextLayout = { layoutResult = it },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .pointerInput(hits) {
                                    detectTapGestures { pos ->
                                        val layout = layoutResult ?: return@detectTapGestures
                                        val offset = layout.getOffsetForPosition(pos)
                                        val hit = hits.firstOrNull { occ ->
                                            offset >= occ.start && offset < occ.start + occ.char.length
                                        }
                                        if (hit != null) {
                                            val note = piece.annotations.firstOrNull {
                                                it.word == hit.char && it.meaning == hit.yixiang
                                            }?.note.orEmpty()
                                            tapped = TapInfo(
                                                word = hit.char,
                                                meaning = hit.yixiang,
                                                note = note,
                                                isSame = hit.yixiang == meaning
                                            )
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp)
                        )
                    }
                    // 译文
                    1 -> Text(
                        text = piece.translation.ifBlank { "暂无译文" },
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    )
                    // 注释
                    else -> Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        if (piece.annotations.isEmpty()) {
                            Text("暂无注释", color = colors.textSecondary, fontSize = 14.sp)
                        }
                        piece.annotations.forEach { annotation ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(annotation.word, color = colors.accent, fontSize = 15.sp)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    annotation.meaning,
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp
                                )
                            }
                            if (annotation.note.isNotBlank()) {
                                Text(
                                    text = annotation.note,
                                    color = colors.textSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    // 点击标记词后弹出该处释义
    tapped?.let { info ->
        AlertDialog(
            onDismissRequest = { tapped = null },
            containerColor = colors.panel,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = info.word,
                        color = colors.accent,
                        fontFamily = PixelFontFamily,
                        fontSize = 26.sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier
                            .size(14.dp)
                            .background(if (info.isSame) colors.sameHighlight else colors.diffHighlight)
                            .border(1.dp, colors.panelBorder)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "此处释义：${info.meaning}",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                    if (info.note.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = info.note,
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (info.isSame) "与当前查看的义项相同" else "当前卡片该字的其他义项",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { tapped = null }) {
                    Text("知道了", color = colors.accent, fontFamily = PixelFontFamily)
                }
            }
        )
    }
}

// 图例小方块
@Composable
private fun LegendBox(color: Color, label: String) {
    val colors = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(14.dp)
                .background(color)
                .border(1.dp, colors.panelBorder)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = colors.textSecondary, fontSize = 12.sp)
    }
}

// 构建高亮正文：只标注当前卡片的字；同义一色、异义另一色，加粗以突出
private fun buildHighlighted(
    piece: TextPiece,
    highlightChar: String,
    meaning: String,
    hits: List<Occurrence>,
    colors: com.ziyuan.wenyan.ui.theme.AppColors
) = buildAnnotatedString {
    append(piece.content)
    hits.forEach { occurrence ->
        val start = occurrence.start
        val end = occurrence.start + occurrence.char.length
        if (start >= 0 && end <= piece.content.length) {
            addStyle(
                style = SpanStyle(
                    background = if (occurrence.yixiang == meaning) colors.sameHighlight else colors.diffHighlight,
                    color = colors.highlightText,
                    fontWeight = FontWeight.Bold
                ),
                start = start,
                end = end
            )
        }
    }
}
