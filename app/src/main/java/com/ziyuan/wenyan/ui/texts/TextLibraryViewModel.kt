package com.ziyuan.wenyan.ui.texts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ziyuan.wenyan.data.model.TextPiece
import com.ziyuan.wenyan.data.repository.TextRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 按教材册别分组后的篇目组（组内保持 DAO 排序：册别 + 标题）
data class TextGroup(
    val book: String,
    val pieces: List<TextPiece>
)

@HiltViewModel
class TextLibraryViewModel @Inject constructor(
    private val textRepository: TextRepository
) : ViewModel() {

    // 固定分组顺序，未列出的册别排在最后
    private val bookOrder = listOf(
        "必修上册",
        "必修下册",
        "选择性必修上册",
        "选择性必修中册",
        "选择性必修下册"
    )

    // 搜索关键字（输入框状态）
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // 搜索结果：null 表示分组浏览态，非 null 表示搜索结果态
    private val _searchResults = MutableStateFlow<List<TextPiece>?>(null)
    val searchResults: StateFlow<List<TextPiece>?> = _searchResults.asStateFlow()

    // 分组浏览数据：null 表示尚未加载完成（避免首帧误显空态）
    val groups: StateFlow<List<TextGroup>?> = textRepository.observeAllTexts()
        .map { list -> groupByBook(list) }
        .stateIn<List<TextGroup>?>(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 输入变化：清空输入时回到分组浏览态
    fun onQueryChange(value: String) {
        _query.value = value
        if (value.isBlank()) _searchResults.value = null
    }

    // 点击搜索按钮：按标题 / 作者检索
    fun search() {
        val q = _query.value.trim()
        if (q.isEmpty()) {
            _searchResults.value = null
            return
        }
        viewModelScope.launch {
            _searchResults.value = textRepository.searchTexts(q)
        }
    }

    // 按固定册别顺序分组，其余册别（含空册别）按名称排在最后
    private fun groupByBook(texts: List<TextPiece>): List<TextGroup> {
        val byBook = texts.groupBy { it.book.ifBlank { "其他" } }
        val result = mutableListOf<TextGroup>()
        bookOrder.forEach { book ->
            byBook[book]?.let { result += TextGroup(book, it) }
        }
        byBook.keys.filter { it !in bookOrder }.sorted().forEach { book ->
            result += TextGroup(book, byBook[book].orEmpty())
        }
        return result
    }
}
