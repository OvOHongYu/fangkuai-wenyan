package com.ziyuan.wenyan.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.model.CompareCardFull
import com.ziyuan.wenyan.data.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// 对比卡片页 ViewModel：持有当前 compareId，暴露对比卡数据
@HiltViewModel
class CompareDetailViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    // 当前查看的对比卡 id（页面 LaunchedEffect 时通过 load 注入）
    private val compareIdFlow = MutableStateFlow<String?>(null)

    // 对比卡数据流
    @OptIn(ExperimentalCoroutinesApi::class)
    val card: StateFlow<CompareCardFull?> = compareIdFlow
        .filterNotNull()
        .flatMapLatest { cardRepository.observeCard(it) }
        .map { it as? CompareCardFull }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun load(compareId: String) {
        if (compareIdFlow.value != compareId) compareIdFlow.value = compareId
    }
}
