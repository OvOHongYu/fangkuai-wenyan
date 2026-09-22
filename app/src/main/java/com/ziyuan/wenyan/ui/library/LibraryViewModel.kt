package com.ziyuan.wenyan.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.model.CardUnionFull
import com.ziyuan.wenyan.data.model.CompareCardFull
import com.ziyuan.wenyan.data.model.ShiCardFull
import com.ziyuan.wenyan.data.model.XuCardFull
import com.ziyuan.wenyan.data.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 卡片库 ViewModel：实词 / 虚词 / 对比 / 收藏 四路数据
@HiltViewModel
class LibraryViewModel @Inject constructor(
    cardRepository: CardRepository
) : ViewModel() {

    // 实词列表（后端已按拼音 sortKey 排序）
    val shiCards: StateFlow<List<ShiCardFull>> = cardRepository.observeShiCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 虚词列表（含 category 分类）
    val xuCards: StateFlow<List<XuCardFull>> = cardRepository.observeXuCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 对比卡列表
    val compareCards: StateFlow<List<CompareCardFull>> = cardRepository.observeCompareCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 收藏列表（实词 / 虚词 / 对比 混合）
    val favorites: StateFlow<List<CardUnionFull>> = cardRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
