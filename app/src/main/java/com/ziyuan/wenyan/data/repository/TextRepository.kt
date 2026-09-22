package com.ziyuan.wenyan.data.repository

import com.ziyuan.wenyan.data.local.dao.OccurrenceDao
import com.ziyuan.wenyan.data.local.dao.TextDao
import com.ziyuan.wenyan.data.model.TextPiece
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextRepository @Inject constructor(
    private val textDao: TextDao,
    private val occurrenceDao: OccurrenceDao,
    private val json: Json
) {
    // 全部篇目（UI 端按 book 分组）
    fun observeAllTexts(): Flow<List<TextPiece>> =
        textDao.observeAll().map { list -> list.map { json.decodeFromString<TextPiece>(it.dataJson) } }

    fun observeText(textId: String): Flow<TextPiece?> =
        textDao.observeById(textId).map { it?.let { e -> json.decodeFromString<TextPiece>(e.dataJson) } }

    suspend fun getText(textId: String): TextPiece? =
        textDao.getById(textId)?.let { json.decodeFromString<TextPiece>(it.dataJson) }

    suspend fun searchTexts(query: String): List<TextPiece> =
        textDao.search("%${query.trim()}%").map { json.decodeFromString<TextPiece>(it.dataJson) }

    // 某字某义项出现过的全部篇目
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTextsForOccurrence(char: String, yixiang: String): Flow<List<TextPiece>> =
        occurrenceDao.observeTextIds(char, yixiang).flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf(emptyList())
            else textDao.observeByIds(ids).map { list -> list.map { json.decodeFromString<TextPiece>(it.dataJson) } }
        }
}
