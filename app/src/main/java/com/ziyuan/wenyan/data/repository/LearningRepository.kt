package com.ziyuan.wenyan.data.repository

import com.ziyuan.wenyan.data.local.dao.LearningDao
import com.ziyuan.wenyan.data.local.dao.StatsDao
import com.ziyuan.wenyan.data.local.dao.WrongAnswerDao
import com.ziyuan.wenyan.data.local.entity.DailyStatEntity
import com.ziyuan.wenyan.data.local.entity.LearningRecordEntity
import com.ziyuan.wenyan.data.local.entity.WrongAnswerEntity
import com.ziyuan.wenyan.util.SpacedRepetition
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearningRepository @Inject constructor(
    private val learningDao: LearningDao,
    private val wrongAnswerDao: WrongAnswerDao,
    private val statsDao: StatsDao
) {
    // 学习一张卡片：首次学习即生成遗忘曲线复习计划
    suspend fun markCardLearned(cardId: String) {
        val now = System.currentTimeMillis()
        if (learningDao.getByCardId(cardId) == null) {
            learningDao.upsert(
                LearningRecordEntity(
                    cardId = cardId,
                    stage = 0,
                    learnedAt = now,
                    nextReviewAt = now + SpacedRepetition.intervalMs(0)
                )
            )
        }
        bump { it.copy(cardsLearned = it.cardsLearned + 1) }
    }

    // 完成一次复习：阶段 +1，刷新下次复习时间
    suspend fun markReviewed(cardId: String) {
        val record = learningDao.getByCardId(cardId) ?: return
        val now = System.currentTimeMillis()
        val stage = (record.stage + 1).coerceAtMost(SpacedRepetition.MAX_STAGE)
        learningDao.upsert(
            record.copy(
                stage = stage,
                reviewCount = record.reviewCount + 1,
                nextReviewAt = now + SpacedRepetition.intervalMs(stage)
            )
        )
        bump { it.copy(reviewCount = it.reviewCount + 1) }
    }

    fun observeDueRecords(): Flow<List<LearningRecordEntity>> =
        learningDao.observeDue(System.currentTimeMillis())

    fun observeLearnedCount(): Flow<Int> = learningDao.observeLearnedCount()

    fun observeWrongAnswers(): Flow<List<WrongAnswerEntity>> = wrongAnswerDao.observeAll()

    suspend fun addWrongAnswer(item: WrongAnswerEntity) {
        wrongAnswerDao.insert(item)
    }

    suspend fun markWrongPracticed(id: Long) = wrongAnswerDao.markPracticed(id)

    suspend fun deleteWrongAnswer(id: Long) = wrongAnswerDao.delete(id)

    fun observeStats(): Flow<List<DailyStatEntity>> = statsDao.observeAll()

    suspend fun recordQuizResult(correct: Boolean) {
        bump {
            it.copy(
                quizCount = it.quizCount + 1,
                quizCorrect = it.quizCorrect + if (correct) 1 else 0
            )
        }
    }

    private suspend fun bump(update: (DailyStatEntity) -> DailyStatEntity) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())
        val current = statsDao.getByDate(today) ?: DailyStatEntity(date = today)
        statsDao.upsert(update(current))
    }
}
