package com.ziyuan.wenyan.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.model.CardUnionFull
import com.ziyuan.wenyan.data.model.ShiCardFull
import com.ziyuan.wenyan.data.repository.CardRepository
import com.ziyuan.wenyan.data.repository.LearningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// 卡片详情页 ViewModel：持有当前 cardId，暴露卡片数据 / 相邻卡片 / 收藏 / 已学联动等能力
@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val learningRepository: LearningRepository
) : ViewModel() {

    // 当前查看的卡片 id（页面 LaunchedEffect 时通过 onOpen 注入）
    private val cardIdFlow = MutableStateFlow<String?>(null)

    // 卡片完整数据流（含收藏 / 已学状态）
    @OptIn(ExperimentalCoroutinesApi::class)
    val card: StateFlow<CardUnionFull?> = cardIdFlow
        .filterNotNull()
        .flatMapLatest { cardRepository.observeCard(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 同分类下的上一张 / 下一张卡片 id
    @OptIn(ExperimentalCoroutinesApi::class)
    val neighbors: StateFlow<Pair<String?, String?>> = cardIdFlow
        .filterNotNull()
        .flatMapLatest { flow { emit(cardRepository.neighbors(it)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null to null)

    // 打开页面：切换卡片并标记已学（首次标记才联动复习计划 / 统计）
    fun onOpen(cardId: String) {
        if (cardIdFlow.value == cardId) return
        cardIdFlow.value = cardId
        viewModelScope.launch {
            if (cardRepository.markLearned(cardId)) {
                learningRepository.markCardLearned(cardId)
            }
        }
    }

    // 收藏 / 取消收藏
    fun toggleFavorite() {
        val id = cardIdFlow.value ?: return
        viewModelScope.launch { cardRepository.toggleFavorite(id) }
    }

    // 解析"相关字"：找到 char 精确匹配的实词卡，找不到回调 null（提示未收录）
    fun findRelatedCard(char: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val currentId = cardIdFlow.value
            val found = cardRepository.searchCards(char).firstOrNull {
                it.id != currentId && (it as? ShiCardFull)?.card?.char == char
            }
            onResult(found?.id)
        }
    }

    // 解析虚词"对比辨析"：在对比卡中找 charLeft 或 charRight == 指定字的卡片
    fun findCompareCard(withChar: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val found = cardRepository.observeCompareCards().first().firstOrNull {
                it.card.charLeft == withChar || it.card.charRight == withChar
            }
            onResult(found?.card?.id)
        }
    }
}
