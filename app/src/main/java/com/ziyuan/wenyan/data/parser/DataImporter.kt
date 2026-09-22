package com.ziyuan.wenyan.data.parser

import android.content.Context
import com.ziyuan.wenyan.data.local.AppDatabase
import com.ziyuan.wenyan.data.local.entity.CardEntity
import com.ziyuan.wenyan.data.local.entity.OccurrenceEntity
import com.ziyuan.wenyan.data.local.entity.TextEntity
import com.ziyuan.wenyan.data.model.CompareCard
import com.ziyuan.wenyan.data.model.ShiCard
import com.ziyuan.wenyan.data.model.TextPiece
import com.ziyuan.wenyan.data.model.XuCard
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// 首次启动将 assets 中的 JSON 数据导入 Room；单文件解析失败时跳过，不影响其他数据
@Singleton
class DataImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val json: Json
) {
    suspend fun importIfEmpty() = withContext(Dispatchers.IO) {
        if (db.cardDao().count() > 0 && db.textDao().count() > 0) return@withContext
        importCards()
        importTexts()
    }

    private suspend fun importCards() {
        val cardDao = db.cardDao()
        listAssetFiles("cards").filter { it.endsWith(".json") }.forEach { name ->
            val raw = readAsset("cards/$name") ?: return@forEach
            when {
                name.startsWith("shici") -> runCatching { json.decodeFromString<List<ShiCard>>(raw) }
                    .getOrElse { emptyList() }
                    .forEach {
                        cardDao.upsert(
                            CardEntity(
                                id = it.id, type = "实词", char = it.char, pinyin = it.pinyin,
                                title = null, sortKey = it.pinyin.ifBlank { it.char },
                                dataJson = json.encodeToString(it)
                            )
                        )
                    }
                name.startsWith("xuci") -> runCatching { json.decodeFromString<List<XuCard>>(raw) }
                    .getOrElse { emptyList() }
                    .forEach {
                        cardDao.upsert(
                            CardEntity(
                                id = it.id, type = "虚词", char = it.char, pinyin = it.pinyin,
                                title = null, sortKey = it.category.ifBlank { it.char },
                                dataJson = json.encodeToString(it)
                            )
                        )
                    }
                name.startsWith("compare") -> runCatching { json.decodeFromString<List<CompareCard>>(raw) }
                    .getOrElse { emptyList() }
                    .forEach {
                        cardDao.upsert(
                            CardEntity(
                                id = it.id, type = "对比", char = null, pinyin = null,
                                title = it.title, sortKey = it.title,
                                dataJson = json.encodeToString(it)
                            )
                        )
                    }
            }
        }
    }

    private suspend fun importTexts() {
        val textDao = db.textDao()
        val occurrenceDao = db.occurrenceDao()
        listAssetFiles("texts").filter { it.endsWith(".json") }.forEach { name ->
            val raw = readAsset("texts/$name") ?: return@forEach
            runCatching { json.decodeFromString<List<TextPiece>>(raw) }.getOrElse { emptyList() }
                .forEach { piece ->
                    textDao.upsert(
                        TextEntity(
                            id = piece.id, title = piece.title, author = piece.author,
                            dynasty = piece.dynasty, book = piece.book, genre = piece.genre,
                            dataJson = json.encodeToString(piece)
                        )
                    )
                    occurrenceDao.insertAll(
                        piece.occurrences.map {
                            OccurrenceEntity(
                                textId = piece.id,
                                char = it.char,
                                yixiang = it.yixiang,
                                start = it.start
                            )
                        }
                    )
                }
        }
    }

    private fun readAsset(path: String): String? =
        runCatching { context.assets.open(path).bufferedReader().use { it.readText() } }.getOrNull()

    private fun listAssetFiles(dir: String): List<String> =
        runCatching { context.assets.list(dir)?.toList() ?: emptyList() }.getOrDefault(emptyList())
}
