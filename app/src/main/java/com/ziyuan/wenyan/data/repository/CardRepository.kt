package com.ziyuan.wenyan.data.repository

import com.ziyuan.wenyan.data.local.dao.CardDao
import com.ziyuan.wenyan.data.local.entity.CardEntity
import com.ziyuan.wenyan.data.model.CardUnionFull
import com.ziyuan.wenyan.data.model.CompareCardFull
import com.ziyuan.wenyan.data.model.ShiCardFull
import com.ziyuan.wenyan.data.model.ShiCard
import com.ziyuan.wenyan.data.model.XuCardFull
import com.ziyuan.wenyan.data.model.CompareCard
import com.ziyuan.wenyan.data.model.XuCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao,
    private val json: Json
) {
    fun observeShiCards(): Flow<List<ShiCardFull>> =
        cardDao.observeByType("实词").map { list -> list.map { it.toShiFull() } }

    fun observeXuCards(): Flow<List<XuCardFull>> =
        cardDao.observeByType("虚词").map { list -> list.map { it.toXuFull() } }

    fun observeCompareCards(): Flow<List<CompareCardFull>> =
        cardDao.observeByType("对比").map { list -> list.map { it.toCompareFull() } }

    fun observeFavorites(): Flow<List<CardUnionFull>> =
        cardDao.observeFavorites().map { list -> list.mapNotNull { it.toFull() } }

    fun observeCard(cardId: String): Flow<CardUnionFull?> =
        cardDao.observeById(cardId).map { it?.toFull() }

    suspend fun getCard(cardId: String): CardUnionFull? = cardDao.getById(cardId)?.toFull()

    suspend fun searchCards(query: String): List<CardUnionFull> =
        cardDao.search(query.trim()).mapNotNull { it.toFull() }

    suspend fun randomShiCard(): ShiCardFull? = cardDao.randomByType("实词")?.toShiFull()

    suspend fun toggleFavorite(cardId: String) {
        val card = cardDao.getById(cardId) ?: return
        cardDao.setFavorite(cardId, !card.favorite)
    }

    // 标记卡片已学；返回是否为首次标记（用于联动复习计划与统计）
    suspend fun markLearned(cardId: String): Boolean {
        val card = cardDao.getById(cardId) ?: return false
        if (card.learned) return false
        cardDao.setLearned(cardId, true)
        return true
    }

    // 同一分类下的上一张 / 下一张卡片 id
    suspend fun neighbors(cardId: String): Pair<String?, String?> {
        val card = cardDao.getById(cardId) ?: return null to null
        val prev = cardDao.prevCard(card.type, card.sortKey)?.id
        val next = cardDao.nextCard(card.type, card.sortKey)?.id
        return prev to next
    }

    private fun CardEntity.toShiFull(): ShiCardFull =
        ShiCardFull(json.decodeFromString<ShiCard>(dataJson), favorite, learned)

    private fun CardEntity.toXuFull(): XuCardFull =
        XuCardFull(json.decodeFromString<XuCard>(dataJson), favorite, learned)

    private fun CardEntity.toCompareFull(): CompareCardFull =
        CompareCardFull(json.decodeFromString<CompareCard>(dataJson), favorite, learned)

    private fun CardEntity.toFull(): CardUnionFull? = when (type) {
        "实词" -> toShiFull()
        "虚词" -> toXuFull()
        "对比" -> toCompareFull()
        else -> null
    }
}
