package com.ziyuan.wenyan.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.model.CardUnionFull
import com.ziyuan.wenyan.data.model.ShiCardFull
import com.ziyuan.wenyan.data.model.XuCardFull
import com.ziyuan.wenyan.data.repository.CardRepository
import com.ziyuan.wenyan.data.repository.LearningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// 首页 ViewModel：待复习统计 / 学习进度 / 搜索 / 随机一字
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    learningRepository: LearningRepository
) : ViewModel() {

    // 今日待复习数量（来自遗忘曲线计划）
    val dueCount: StateFlow<Int> = learningRepository.observeDueRecords()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 实词卡列表（含 learned 标记，用于统计学习进度）
    val shiCards: StateFlow<List<ShiCardFull>> = cardRepository.observeShiCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 虚词卡列表（含 learned 标记）
    val xuCards: StateFlow<List<XuCardFull>> = cardRepository.observeXuCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 最近一次搜索的关键词（空表示未搜索）
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 最近一次搜索结果
    private val _searchResults = MutableStateFlow<List<CardUnionFull>>(emptyList())
    val searchResults: StateFlow<List<CardUnionFull>> = _searchResults.asStateFlow()

    // 随机一字结果（null 表示尚未抽取）
    private val _randomCard = MutableStateFlow<ShiCardFull?>(null)
    val randomCard: StateFlow<ShiCardFull?> = _randomCard.asStateFlow()

    // 执行搜索：恰好 1 条时通过回调直接打开该卡片
    fun search(query: String, onSingleResult: (String) -> Unit) {
        val q = query.trim()
        if (q.isEmpty()) return
        viewModelScope.launch {
            _searchQuery.value = q
            val results = cardRepository.searchCards(q)
            _searchResults.value = results
            if (results.size == 1) onSingleResult(results.first().id)
        }
    }

    // 随机抽取一张实词卡（可重复点击重抽）
    fun randomOne() {
        viewModelScope.launch {
            _randomCard.value = cardRepository.randomShiCard()
        }
    }
}
