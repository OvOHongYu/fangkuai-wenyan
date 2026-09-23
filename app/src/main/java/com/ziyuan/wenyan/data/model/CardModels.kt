package com.ziyuan.wenyan.data.model

import kotlinx.serialization.Serializable

// ---------- 实词卡片 ----------
@Serializable
data class ChainStep(
    val from: String,       // 起点（核心画面）
    val to: String,         // 推导出的义项
    val condition: String   // 推导条件
)

@Serializable
data class ExampleSentence(
    val yixiang: String,
    val sentence: String,
    val source: String,
    val translation: String,
    val isGaokao: Boolean = false
)

@Serializable
data class IdiomItem(
    val idiom: String,
    val yixiang: String
)

@Serializable
data class TrapItem(
    val warning: String,
    val gaokao: String? = null
)

@Serializable
data class ShiCard(
    val id: String,
    val type: String = "实词",
    val char: String,
    val pinyin: String,
    val picture: String,
    val benyi: String,
    val chain: List<ChainStep> = emptyList(),
    val examples: List<ExampleSentence> = emptyList(),
    val idioms: List<IdiomItem> = emptyList(),
    val traps: List<TrapItem> = emptyList(),
    val mnemonic: String = "",
    val relatedChars: List<String> = emptyList()
)

// ---------- 虚词卡片 ----------
@Serializable
data class JudgeFlowStep(
    val condition: String,        // 判断条件
    val relation: String,         // 关系/词性
    val translation: String,      // 译法
    val example: String,
    val source: String,
    val translationFull: String
)

@Serializable
data class SpecialNote(
    val title: String,
    val content: String
)

@Serializable
data class ContrastExample(
    val char: String,
    val sentence: String,
    val feature: String
)

@Serializable
data class ContrastBlock(
    val `with`: String,
    val difference: String,
    val examples: List<ContrastExample> = emptyList()
)

@Serializable
data class XuCard(
    val id: String,
    val type: String = "虚词",
    val char: String,
    val pinyin: String,
    val category: String = "",   // 功能分类：连词/介词/代词/语气词
    val picture: String,
    val coreFunction: String,
    val judgeFlow: List<JudgeFlowStep> = emptyList(),
    val specialNotes: List<SpecialNote> = emptyList(),
    val contrast: ContrastBlock? = null,
    val mnemonic: String = "",
    val gaokaoTips: List<String> = emptyList()
)

// ---------- 对比卡片 ----------
@Serializable
data class CompareTableRow(
    val relation: String,
    val left: String,   // 左侧字例句，"✅ xxx" 或 "❌"
    val right: String   // 右侧字例句
)

@Serializable
data class CompareCard(
    val id: String,
    val type: String = "对比",
    val title: String,
    val pinyin: String = "",   // 标题首字拼音，用于卡片库字母索引
    val charLeft: String = "",
    val charRight: String = "",
    val coreDifference: String = "",
    val table: List<CompareTableRow> = emptyList(),
    val oneLine: String = ""
)

// ---------- 统一卡片视图（含收藏 / 已学状态） ----------
sealed interface CardUnionFull {
    val id: String
    val favorite: Boolean
    val learned: Boolean
}

data class ShiCardFull(val card: ShiCard, override val favorite: Boolean, override val learned: Boolean) : CardUnionFull {
    override val id: String get() = card.id
}

data class XuCardFull(val card: XuCard, override val favorite: Boolean, override val learned: Boolean) : CardUnionFull {
    override val id: String get() = card.id
}

data class CompareCardFull(val card: CompareCard, override val favorite: Boolean, override val learned: Boolean) : CardUnionFull {
    override val id: String get() = card.id
}
