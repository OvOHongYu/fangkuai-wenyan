package com.ziyuan.wenyan.ui.wrongbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.repository.LearningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// 错题列表项
data class WrongBookItem(
    val id: Long,
    val cardId: String?,
    val question: String,
    val correctAnswer: String,
    val userAnswer: String,
    val analysis: String,
    val source: String,
    val timeText: String,     // yyyy-MM-dd HH:mm
    val practiced: Boolean
)

@HiltViewModel
class WrongBookViewModel @Inject constructor(
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

    // 错题列表（按时间倒序）
    val items: StateFlow<List<WrongBookItem>> = learningRepository.observeWrongAnswers()
        .map { list ->
            list.map { entity ->
                WrongBookItem(
                    id = entity.id,
                    cardId = entity.cardId,
                    question = entity.question,
                    correctAnswer = entity.correctAnswer,
                    userAnswer = entity.userAnswer,
                    analysis = entity.analysis,
                    source = entity.source,
                    timeText = timeFormat.format(Date(entity.createdAt)),
                    practiced = entity.practiced
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 删除一条错题
    fun delete(id: Long) {
        viewModelScope.launch { learningRepository.deleteWrongAnswer(id) }
    }
}
