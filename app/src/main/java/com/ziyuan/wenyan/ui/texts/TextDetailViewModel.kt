package com.ziyuan.wenyan.ui.texts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.model.TextPiece
import com.ziyuan.wenyan.data.repository.TextRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// 篇目详情 UI 状态
sealed interface TextDetailUiState {
    data object Loading : TextDetailUiState   // 加载中
    data class Ready(val piece: TextPiece) : TextDetailUiState  // 加载成功
    data object NotFound : TextDetailUiState  // 未找到该篇目
}

@HiltViewModel
class TextDetailViewModel @Inject constructor(
    private val textRepository: TextRepository
) : ViewModel() {

    // 当前展示的篇目 id
    private val textIdFlow = MutableStateFlow("")

    // 篇目详情状态（跟随数据库实时更新）
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TextDetailUiState> = textIdFlow
        .flatMapLatest { id ->
            if (id.isBlank()) {
                flowOf(TextDetailUiState.Loading)
            } else {
                textRepository.observeText(id).map { piece ->
                    if (piece == null) TextDetailUiState.NotFound
                    else TextDetailUiState.Ready(piece)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TextDetailUiState.Loading)

    // 绑定要展示的篇目 id（页面进入时调用）
    fun bind(textId: String) {
        textIdFlow.value = textId
    }
}
