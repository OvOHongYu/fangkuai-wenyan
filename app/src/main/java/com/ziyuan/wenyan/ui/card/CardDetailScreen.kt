package com.ziyuan.wenyan.ui.card

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ziyuan.wenyan.data.model.CompareCard
import com.ziyuan.wenyan.data.model.CompareCardFull
import com.ziyuan.wenyan.data.model.ShiCard
import com.ziyuan.wenyan.data.model.ShiCardFull
import com.ziyuan.wenyan.data.model.XuCard
import com.ziyuan.wenyan.data.model.XuCardFull
import com.ziyuan.wenyan.ui.components.OreBackground
import com.ziyuan.wenyan.ui.components.OreButton
import com.ziyuan.wenyan.ui.components.OreButtonVariant
import com.ziyuan.wenyan.ui.components.OreChip
import com.ziyuan.wenyan.ui.components.OrePanel
import com.ziyuan.wenyan.ui.components.OreSectionTitle
import com.ziyuan.wenyan.ui.components.OreTopBar
import com.ziyuan.wenyan.ui.texts.TextReadingSheet
import com.ziyuan.wenyan.ui.theme.AppTheme
import com.ziyuan.wenyan.ui.theme.OreGreen
import com.ziyuan.wenyan.ui.theme.OrePanelBorder
import com.ziyuan.wenyan.ui.theme.OreTextPrimary
import com.ziyuan.wenyan.ui.theme.OreTextSecondary
import com.ziyuan.wenyan.ui.theme.OreYellow
import com.ziyuan.wenyan.ui.theme.PixelFontFamily

// 卡片详情页（实词 8 区块 / 虚词 7 区块），含"出现篇目"弹窗与定位阅读窗口
@Composable
fun CardDetailScreen(
    cardId: String,
    onBack: () -> Unit,
    onOpenCard: (String) -> Unit,
    onOpenCompare: (String) -> Unit,
    onStartQuiz: () -> Unit
) {
    val viewModel: CardDetailViewModel = hiltViewModel()
    val card by viewModel.card.collectAsStateWithLifecycle()
    val neighbors by viewModel.neighbors.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 打开页面即切换卡片并标记已学（首次标记才联动复习计划 / 统计）
    LaunchedEffect(cardId) { viewModel.onOpen(cardId) }

    var showChainDialog by remember { mutableStateOf(false) }
    // 待打开的"出现篇目"弹窗：字 to 义项
    var occurrenceTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    // 定位阅读窗口：textId / 高亮字 / 义项名
    var readingTarget by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    OreBackground {
        Column(Modifier.fillMaxSize()) {
            OreTopBar(
                title = "详情",
                modifier = Modifier.statusBarsPadding(),
                onBack = onBack
            ) {
                val favorite = card?.favorite == true
                Text(
                    text = if (favorite) "★" else "☆",
                    color = if (favorite) OreYellow else OreTextSecondary,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { viewModel.toggleFavorite() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // 内容区（可滚动）
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                when (val current = card) {
                    null -> Box(
                        Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("加载中…", color = OreTextSecondary, fontSize = 14.sp)
                    }
                    is ShiCardFull -> ShiSections(
                        card = current.card,
                        onShowChain = { showChainDialog = true },
                        onOpenOccurrence = { ch, yx -> occurrenceTarget = ch to yx },
                        onRelatedChar = { ch ->
                            viewModel.findRelatedCard(ch) { id ->
                                if (id != null) onOpenCard(id)
                                else Toast.makeText(context, "「$ch」未收录", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onStartQuiz = onStartQuiz
                    )
                    is XuCardFull -> XuSections(
                        card = current.card,
                        onOpenOccurrence = { ch, yx -> occurrenceTarget = ch to yx },
                        onOpenCompare = { withChar ->
                            viewModel.findCompareCard(withChar) { id ->
                                if (id != null) onOpenCompare(id)
                                else Toast.makeText(context, "未找到对应对比卡片", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    is CompareCardFull -> CompareBrief(
                        card = current.card,
                        onOpenCompare = onOpenCompare
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // 底部固定操作行：上一字 / 下一字 / 实战检测
            Column(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OrePanelBorder)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.topBar)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OreButton(
                        text = "上一字",
                        onClick = { neighbors.first?.let(onOpenCard) },
                        modifier = Modifier.weight(1f),
                        variant = OreButtonVariant.SECONDARY,
                        enabled = neighbors.first != null
                    )
                    OreButton(
                        text = "下一字",
                        onClick = { neighbors.second?.let(onOpenCard) },
                        modifier = Modifier.weight(1f),
                        variant = OreButtonVariant.SECONDARY,
                        enabled = neighbors.second != null
                    )
                    OreButton(
                        text = "实战检测",
                        onClick = onStartQuiz,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // 推导链动画弹窗
    if (showChainDialog && card is ShiCardFull) {
        ChainAnimationDialog(
            card = (card as ShiCardFull).card,
            onDismiss = { showChainDialog = false }
        )
    }

    // "出现篇目"选择弹窗
    occurrenceTarget?.let { (ch, yx) ->
        OccurrenceTextsDialog(
            char = ch,
            yixiang = yx,
            onDismiss = { occurrenceTarget = null },
            onOpenText = { textId ->
                occurrenceTarget = null
                readingTarget = Triple(textId, ch, yx)
            }
        )
    }

    // 定位阅读窗口（3/4 屏底部弹窗）
    readingTarget?.let { (textId, ch, mx) ->
        TextReadingSheet(
            textId = textId,
            highlightChar = ch,
            meaning = mx,
            onDismiss = { readingTarget = null }
        )
    }
}

// ---------- 通用小组件 ----------

// 区块：序号标题 + 面板
@Composable
private fun Section(index: Int, title: String, content: @Composable ColumnScope.() -> Unit) {
    OreSectionTitle(index = index, title = title)
    Spacer(Modifier.height(8.dp))
    OrePanel(content = content)
    Spacer(Modifier.height(14.dp))
}

// 顶部头部面板：大字 + 拼音 + 类型 Chip
@Composable
private fun CardHeader(char: String, pinyin: String, type: String) {
    OrePanel(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = char,
                color = OreTextPrimary,
                fontFamily = PixelFontFamily,
                fontSize = 48.sp
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = pinyin, color = OreTextSecondary, fontSize = 15.sp)
                Spacer(Modifier.width(10.dp))
                OreChip(text = type, selected = true)
            }
        }
    }
    Spacer(Modifier.height(14.dp))
}

// 高考真题小标签（黄色强调）
@Composable
private fun GaokaoTag() {
    Text(
        text = "🎯 高考真题",
        color = OreYellow,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AppTheme.colors.warning.copy(alpha = 0.22f))
            .border(1.dp, OreYellow.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

// ---------- 实词卡片 8 区块 ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiSections(
    card: ShiCard,
    onShowChain: () -> Unit,
    onOpenOccurrence: (String, String) -> Unit,
    onRelatedChar: (String) -> Unit,
    onStartQuiz: () -> Unit
) {
    CardHeader(char = card.char, pinyin = card.pinyin, type = card.type)

    // ① 一句话画面
    Section(index = 0, title = "一句话画面") {
        Text(card.picture, color = OreTextPrimary, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(10.dp))
        // 字形演变占位（敬请期待）
        OreChip(text = "字形演变 · 敬请期待")
    }

    // ② 本义
    Section(index = 1, title = "本义") {
        Text(card.benyi, color = OreTextPrimary, style = MaterialTheme.typography.bodyLarge)
    }

    // ③ 推导链
    Section(index = 2, title = "推导链") {
        // 核心画面节点（绿色边框强调）
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AppTheme.colors.field)
                .border(1.dp, OreGreen, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text("核心画面", color = OreGreen, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(card.picture, color = OreTextPrimary, fontSize = 15.sp, lineHeight = 22.sp)
        }
        Spacer(Modifier.height(10.dp))
        card.chain.forEach { step ->
            Row(
                Modifier.padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("├─", color = OreGreen, fontSize = 13.sp)
                Spacer(Modifier.width(6.dp))
                Text(step.condition, color = OreTextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(Modifier.width(6.dp))
                Text("→", color = OreTextSecondary, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(step.to, color = OreGreen, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
        if (card.chain.isEmpty()) {
            Text("暂无推导链", color = OreTextSecondary, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        OreButton(
            text = "▶ 动画演示推导过程",
            onClick = onShowChain,
            modifier = Modifier.fillMaxWidth(),
            variant = OreButtonVariant.SECONDARY
        )
    }

    // ④ 例句表：按义项分组，组头可跳转"出现篇目"
    Section(index = 3, title = "例句表") {
        val grouped = remember(card.examples) { card.examples.groupBy { it.yixiang } }
        grouped.forEach { (yixiang, sentences) ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OreChip(text = yixiang, selected = true)
                Spacer(Modifier.weight(1f))
                OreChip(
                    text = "出现篇目 ▸",
                    onClick = { onOpenOccurrence(card.char, yixiang) }
                )
            }
            Spacer(Modifier.height(8.dp))
            sentences.forEach { example ->
                Column(Modifier.padding(bottom = 10.dp)) {
                    Text(example.sentence, color = OreTextPrimary, fontSize = 15.sp, lineHeight = 24.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(example.source, color = OreTextSecondary, fontSize = 12.sp)
                    Text("译：${example.translation}", color = OreTextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
                    if (example.isGaokao) {
                        Spacer(Modifier.height(4.dp))
                        GaokaoTag()
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        if (card.examples.isEmpty()) {
            Text("暂无例句", color = OreTextSecondary, fontSize = 13.sp)
        }
    }

    // ⑤ 易错辨析
    Section(index = 4, title = "易错辨析") {
        card.traps.forEach { trap ->
            Text("⚠ ${trap.warning}", color = OreTextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
            trap.gaokao?.let { note ->
                Spacer(Modifier.height(4.dp))
                Text("🎯 $note", color = OreYellow, fontSize = 13.sp, lineHeight = 19.sp)
            }
            Spacer(Modifier.height(10.dp))
        }
        if (card.traps.isEmpty()) {
            Text("暂无易错提示", color = OreTextSecondary, fontSize = 13.sp)
        }
    }

    // ⑥ 成语验证
    Section(index = 5, title = "成语验证") {
        card.idioms.forEach { idiom ->
            Text("${idiom.idiom}（${idiom.yixiang}）", color = OreTextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(6.dp))
        }
        if (card.idioms.isEmpty()) {
            Text("暂无成语", color = OreTextSecondary, fontSize = 13.sp)
        }
    }

    // ⑦ 记忆口诀
    Section(index = 6, title = "记忆口诀") {
        Text(
            text = card.mnemonic.ifBlank { "暂无口诀" },
            color = OreGreen,
            fontFamily = PixelFontFamily,
            fontSize = 16.sp,
            lineHeight = 26.sp
        )
    }

    // ⑧ 实战检测 + 相关字
    Section(index = 7, title = "实战检测") {
        Text("相关字", color = OreTextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        if (card.relatedChars.isEmpty()) {
            Text("暂无相关字", color = OreTextSecondary, fontSize = 13.sp)
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                card.relatedChars.forEach { ch ->
                    OreChip(text = ch, onClick = { onRelatedChar(ch) })
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OreButton(text = "开始检测", onClick = onStartQuiz, modifier = Modifier.fillMaxWidth())
    }
}

// ---------- 虚词卡片区块 ----------

@Composable
private fun XuSections(
    card: XuCard,
    onOpenOccurrence: (String, String) -> Unit,
    onOpenCompare: (String) -> Unit
) {
    CardHeader(
        char = card.char,
        pinyin = card.pinyin,
        type = if (card.category.isBlank()) card.type else "${card.type} · ${card.category}"
    )

    // ① 一句话画面
    Section(index = 0, title = "一句话画面") {
        Text(card.picture, color = OreTextPrimary, style = MaterialTheme.typography.bodyLarge)
    }

    // ② 核心功能
    Section(index = 1, title = "核心功能") {
        Text(card.coreFunction, color = OreTextPrimary, style = MaterialTheme.typography.bodyLarge)
    }

    // ③ 判断流程：每步一张小卡
    Section(index = 2, title = "判断流程") {
        if (card.judgeFlow.isEmpty()) {
            Text("暂无判断流程", color = OreTextSecondary, fontSize = 13.sp)
        }
        card.judgeFlow.forEach { step ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppTheme.colors.field)
                    .border(1.dp, OrePanelBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("【${step.condition}】", color = OreGreen, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("→ ${step.relation}", color = OreTextPrimary, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("（${step.translation}）", color = OreTextSecondary, fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(step.example, color = OreTextPrimary, fontSize = 15.sp, lineHeight = 24.sp)
                Text(step.source, color = OreTextSecondary, fontSize = 12.sp)
                Text("全句译：${step.translationFull}", color = OreTextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(8.dp))
                OreChip(
                    text = "出现篇目 ▸",
                    onClick = { onOpenOccurrence(card.char, step.relation) }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    // ④ 特殊用法（如宾语前置）
    Section(index = 3, title = "特殊用法") {
        if (card.specialNotes.isEmpty()) {
            Text("暂无特殊用法", color = OreTextSecondary, fontSize = 13.sp)
        }
        card.specialNotes.forEach { note ->
            Text(note.title, color = OreGreen, fontSize = 14.sp)
            Spacer(Modifier.height(2.dp))
            Text(note.content, color = OreTextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(10.dp))
        }
    }

    // ⑤ 对比辨析（有 contrast 时显示）
    card.contrast?.let { contrast ->
        Section(index = 4, title = "对比辨析") {
            Text("与「${contrast.with}」对比", color = OreTextPrimary, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Text(contrast.difference, color = OreTextSecondary, fontSize = 14.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(12.dp))
            OreButton(
                text = "查看完整对比",
                onClick = { onOpenCompare(contrast.with) },
                modifier = Modifier.fillMaxWidth(),
                variant = OreButtonVariant.SECONDARY
            )
        }
    }

    // ⑥ 高考提示
    if (card.gaokaoTips.isNotEmpty()) {
        Section(index = 5, title = "高考提示") {
            card.gaokaoTips.forEach { tip ->
                Text("🎯 $tip", color = OreTextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
                Spacer(Modifier.height(6.dp))
            }
        }
    }

    // ⑦ 记忆口诀
    Section(index = 6, title = "记忆口诀") {
        Text(
            text = card.mnemonic.ifBlank { "暂无口诀" },
            color = OreGreen,
            fontFamily = PixelFontFamily,
            fontSize = 16.sp,
            lineHeight = 26.sp
        )
    }
}

// 对比卡被打开时的简要展示（正常流程走 CompareDetailScreen）
@Composable
private fun CompareBrief(card: CompareCard, onOpenCompare: (String) -> Unit) {
    OrePanel(Modifier.fillMaxWidth()) {
        Text(
            text = card.title,
            color = OreTextPrimary,
            fontFamily = PixelFontFamily,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(card.coreDifference, color = OreTextSecondary, fontSize = 14.sp, lineHeight = 21.sp)
        Spacer(Modifier.height(12.dp))
        OreButton(
            text = "查看对比详情",
            onClick = { onOpenCompare(card.id) },
            modifier = Modifier.fillMaxWidth(),
            variant = OreButtonVariant.SECONDARY
        )
    }
    Spacer(Modifier.height(14.dp))
}
