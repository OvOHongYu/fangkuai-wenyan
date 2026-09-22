package com.ziyuan.wenyan.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.local.entity.WrongAnswerEntity
import com.ziyuan.wenyan.data.model.CardUnionFull
import com.ziyuan.wenyan.data.repository.CardRepository
import com.ziyuan.wenyan.data.repository.LearningRepository
import com.ziyuan.wenyan.util.QuizGenerator
import com.ziyuan.wenyan.util.QuizQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// 检测页状态
data class QuizUiState(
    val loading: Boolean = true,
    val questions: List<QuizQuestion> = emptyList(),
    val notice: String? = null,        // 页头提示（如：该卡片题目不足，已用全部卡片出题）
    val currentIndex: Int = 0,
    val selectedIndex: Int? = null,
    val submitted: Boolean = false,
    val correctCount: Int = 0,
    val finished: Boolean = false,
    val empty: Boolean = false
) {
    val total: Int get() = questions.size
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val learningRepository: LearningRepository,
    private val json: Json
) : ViewModel() {

    // 检测模式：daily / compare / card / review / wrong（未知值按每日检测处理）
    val mode: String = savedStateHandle.get<String>("mode") ?: "daily"

    private val sourceId: String? = savedStateHandle.get<String>("sourceId")
        ?.takeIf { it.isNotBlank() && !it.contains("{") }

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // wrong 模式下：题目索引 → 对应错题记录 id
    private var wrongIds: Map<Int, Long> = emptyMap()

    init {
        load()
    }

    // 按模式出题（重建题组）
    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, notice = null) }
            val result = loadQuestions()
            wrongIds = result.wrongIds
            _uiState.value = QuizUiState(
                loading = false,
                questions = result.questions,
                notice = result.notice,
                empty = result.questions.isEmpty()
            )
        }
    }

    private data class LoadResult(
        val questions: List<QuizQuestion>,
        val notice: String? = null,
        val wrongIds: Map<Int, Long> = emptyMap()
    )

    private suspend fun loadQuestions(): LoadResult = when (mode) {
        "compare" -> {
            // 只取指定对比卡出题
            val card = sourceId?.let { cardRepository.getCard(it) }
            val questions = if (card != null) QuizGenerator.generate(listOf(card), 5) else emptyList()
            LoadResult(questions)
        }
        "card" -> {
            // 先用单卡出题；出不了则退化为全量卡片并提示
            val card = sourceId?.let { cardRepository.getCard(it) }
            val single = card?.let { QuizGenerator.generate(listOf(it), 5) } ?: emptyList()
            if (single.isNotEmpty()) {
                LoadResult(single)
            } else {
                val all = QuizGenerator.generate(allCards(), 5)
                LoadResult(all, notice = if (all.isNotEmpty()) "该卡片题目不足，已用全部卡片出题" else null)
            }
        }
        "review" -> {
            // 用到期复习卡片出题
            val due = learningRepository.observeDueRecords().first()
            val cards = due.mapNotNull { cardRepository.getCard(it.cardId) }
            LoadResult(QuizGenerator.generate(cards, 5))
        }
        "wrong" -> {
            // 未重练的错题逐条转成题目（不经 QuizGenerator）
            val wrongList = learningRepository.observeWrongAnswers().first().filter { !it.practiced }
            val questions = mutableListOf<QuizQuestion>()
            val ids = mutableMapOf<Int, Long>()
            wrongList.forEach { entity ->
                val options = runCatching { json.decodeFromString<List<String>>(entity.options) }
                    .getOrDefault(emptyList())
                val correctIndex = options.indexOf(entity.correctAnswer)
                if (options.isEmpty() || correctIndex < 0) return@forEach
                ids[questions.size] = entity.id
                questions += QuizQuestion(
                    cardId = entity.cardId,
                    question = entity.question,
                    options = options,
                    correctIndex = correctIndex,
                    analysis = entity.analysis,
                    sentence = entity.sentence,
                    source = entity.source
                )
            }
            LoadResult(questions, wrongIds = ids)
        }
        else -> LoadResult(QuizGenerator.generate(allCards(), 5))
    }

    // 三类卡片合并为全量卡片
    private suspend fun allCards(): List<CardUnionFull> {
        val cards = ArrayList<CardUnionFull>()
        cards += cardRepository.observeShiCards().first()
        cards += cardRepository.observeXuCards().first()
        cards += cardRepository.observeCompareCards().first()
        return cards
    }

    // 选择选项
    fun select(index: Int) {
        _uiState.update { if (it.submitted) it else it.copy(selectedIndex = index) }
    }

    // 提交当前题答案：记录统计 / 错题 / 错题重练标记
    fun submit() {
        val state = _uiState.value
        val selected = state.selectedIndex ?: return
        if (state.submitted) return
        val question = state.questions.getOrNull(state.currentIndex) ?: return
        val correct = selected == question.correctIndex
        viewModelScope.launch {
            learningRepository.recordQuizResult(correct)
            if (mode == "wrong") {
                // 错题重练：标记已练
                wrongIds[state.currentIndex]?.let { learningRepository.markWrongPracticed(it) }
            } else if (!correct) {
                // 答错记入错题本（含 compare 模式）
                learningRepository.addWrongAnswer(
                    WrongAnswerEntity(
                        cardId = question.cardId,
                        question = question.question,
                        options = json.encodeToString(question.options),
                        userAnswer = question.options.getOrNull(selected) ?: "",
                        correctAnswer = question.options.getOrNull(question.correctIndex) ?: "",
                        analysis = question.analysis,
                        sentence = question.sentence,
                        source = question.source,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
            _uiState.update {
                it.copy(submitted = true, correctCount = it.correctCount + if (correct) 1 else 0)
            }
        }
    }

    // 下一题 / 完成本组
    fun next() {
        val state = _uiState.value
        if (!state.submitted) return
        if (state.currentIndex + 1 >= state.questions.size) {
            _uiState.update { it.copy(finished = true) }
        } else {
            _uiState.update {
                it.copy(currentIndex = it.currentIndex + 1, selectedIndex = null, submitted = false)
            }
        }
    }

    // 再来一组：重置进度并重新出题
    fun restart() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, notice = null) }
            val result = loadQuestions()
            if (result.questions.isEmpty() && mode == "wrong") {
                // 错题已全部重练：沿用本组题目，仅重置进度
                _uiState.update {
                    it.copy(
                        loading = false,
                        currentIndex = 0,
                        selectedIndex = null,
                        submitted = false,
                        correctCount = 0,
                        finished = false,
                        empty = false
                    )
                }
            } else {
                wrongIds = result.wrongIds
                _uiState.value = QuizUiState(
                    loading = false,
                    questions = result.questions,
                    notice = result.notice,
                    empty = result.questions.isEmpty()
                )
            }
        }
    }
}
