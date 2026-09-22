package com.ziyuan.wenyan.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.local.entity.DailyStatEntity
import com.ziyuan.wenyan.data.repository.CardRepository
import com.ziyuan.wenyan.data.repository.LearningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

// 单日柱状数据
data class DayBar(
    val label: String,    // 周一 ~ 周日
    val value: Int,       // 当日学习量（学卡 + 复习 + 答题）
    val isToday: Boolean
)

// 我的页统计状态
data class ProfileUiState(
    val checkInDays: Int = 0,         // 打卡天数（有记录的天数）
    val learnedCards: Int = 0,        // 已学卡片
    val accuracyText: String = "--",  // 平均正确率
    val totalReviews: Int = 0,        // 累计复习
    val shiLearned: Int = 0,
    val shiTotal: Int = 0,
    val xuLearned: Int = 0,
    val xuTotal: Int = 0,
    val last7Days: List<DayBar> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    learningRepository: LearningRepository,
    cardRepository: CardRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        learningRepository.observeStats(),
        learningRepository.observeLearnedCount(),
        cardRepository.observeShiCards(),
        cardRepository.observeXuCards()
    ) { stats, learnedCount, shiCards, xuCards ->
        val quizCount = stats.sumOf { it.quizCount }
        val quizCorrect = stats.sumOf { it.quizCorrect }
        ProfileUiState(
            checkInDays = stats.size,
            learnedCards = learnedCount,
            accuracyText = if (quizCount == 0) "--" else {
                "${(quizCorrect * 100.0 / quizCount).roundToInt()}%"
            },
            totalReviews = stats.sumOf { it.reviewCount },
            shiLearned = shiCards.count { it.learned },
            shiTotal = shiCards.size,
            xuLearned = xuCards.count { it.learned },
            xuTotal = xuCards.size,
            last7Days = buildLast7(stats)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    // 近 7 天（含今天）逐日学习量
    private fun buildLast7(stats: List<DailyStatEntity>): List<DayBar> {
        val byDate = stats.associateBy { it.date }
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val weekNames = arrayOf("日", "一", "二", "三", "四", "五", "六")
        val result = mutableListOf<DayBar>()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        repeat(7) { index ->
            val dateText = format.format(calendar.time)
            val stat = byDate[dateText]
            result += DayBar(
                label = "周${weekNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]}",
                value = (stat?.cardsLearned ?: 0) + (stat?.reviewCount ?: 0) + (stat?.quizCount ?: 0),
                isToday = index == 6
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }
}
