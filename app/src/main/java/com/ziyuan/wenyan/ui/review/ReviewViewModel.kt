package com.ziyuan.wenyan.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.model.CardUnionFull
import com.ziyuan.wenyan.data.model.CompareCardFull
import com.ziyuan.wenyan.data.model.ShiCardFull
import com.ziyuan.wenyan.data.model.XuCardFull
import com.ziyuan.wenyan.data.repository.CardRepository
import com.ziyuan.wenyan.data.repository.LearningRepository
import com.ziyuan.wenyan.util.SpacedRepetition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// 复习列表项
data class ReviewItem(
    val cardId: String,
    val title: String,        // 字 / 对比卡标题
    val stage: Int,           // 当前阶段（0 起）
    val intervalText: String  // 下次复习间隔说明
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val learningRepository: LearningRepository,
    cardRepository: CardRepository
) : ViewModel() {

    // 到期复习卡片列表
    val items: StateFlow<List<ReviewItem>> = learningRepository.observeDueRecords()
        .map { records ->
            records.mapNotNull { record ->
                cardRepository.getCard(record.cardId)?.let { card ->
                    ReviewItem(
                        cardId = record.cardId,
                        title = card.displayTitle(),
                        stage = record.stage,
                        intervalText = formatInterval(
                            SpacedRepetition.INTERVALS_MS[
                                record.stage.coerceIn(0, SpacedRepetition.MAX_STAGE)
                            ]
                        )
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 完成一次复习（完成后该项自动从列表消失）
    fun markReviewed(cardId: String) {
        viewModelScope.launch { learningRepository.markReviewed(cardId) }
    }

    // 卡片显示名：实词/虚词取字，对比卡取标题
    private fun CardUnionFull.displayTitle(): String = when (this) {
        is ShiCardFull -> card.char
        is XuCardFull -> card.char
        is CompareCardFull -> card.title
    }

    companion object {
        // 间隔毫秒 → 人类可读文本（10分钟 / 1天 / …）
        fun formatInterval(ms: Long): String = when {
            ms < 3_600_000L -> "${ms / 60_000L}分钟"
            ms < 86_400_000L -> "${ms / 3_600_000L}小时"
            else -> "${ms / 86_400_000L}天"
        }
    }
}
