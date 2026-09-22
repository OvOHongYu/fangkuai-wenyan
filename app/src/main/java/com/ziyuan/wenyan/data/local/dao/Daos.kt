package com.ziyuan.wenyan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.ziyuan.wenyan.data.local.entity.CardEntity
import com.ziyuan.wenyan.data.local.entity.DailyStatEntity
import com.ziyuan.wenyan.data.local.entity.LearningRecordEntity
import com.ziyuan.wenyan.data.local.entity.OccurrenceEntity
import com.ziyuan.wenyan.data.local.entity.TextEntity
import com.ziyuan.wenyan.data.local.entity.WrongAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY sortKey")
    fun observeAll(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE type = :type ORDER BY sortKey")
    fun observeByType(type: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE favorite = 1 ORDER BY sortKey")
    fun observeFavorites(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun observeById(id: String): Flow<CardEntity?>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: String): CardEntity?

    @Query("SELECT * FROM cards WHERE type = :type ORDER BY RANDOM() LIMIT 1")
    suspend fun randomByType(type: String): CardEntity?

    @Query("SELECT * FROM cards WHERE type = :type AND sortKey < :sortKey ORDER BY sortKey DESC LIMIT 1")
    suspend fun prevCard(type: String, sortKey: String): CardEntity?

    @Query("SELECT * FROM cards WHERE type = :type AND sortKey > :sortKey ORDER BY sortKey ASC LIMIT 1")
    suspend fun nextCard(type: String, sortKey: String): CardEntity?

    @Query("SELECT * FROM cards WHERE char LIKE '%' || :q || '%' OR title LIKE '%' || :q || '%' OR pinyin LIKE '%' || :q || '%' ORDER BY sortKey")
    suspend fun search(q: String): List<CardEntity>

    @Query("SELECT * FROM cards WHERE type = :type ORDER BY sortKey")
    suspend fun listByType(type: String): List<CardEntity>

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(card: CardEntity)

    @Upsert
    suspend fun upsertAll(cards: List<CardEntity>)

    @Query("UPDATE cards SET favorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: String, fav: Boolean)

    @Query("UPDATE cards SET learned = :learned WHERE id = :id")
    suspend fun setLearned(id: String, learned: Boolean)
}

@Dao
interface TextDao {
    @Query("SELECT * FROM texts ORDER BY book, title")
    fun observeAll(): Flow<List<TextEntity>>

    @Query("SELECT * FROM texts WHERE id IN (:ids) ORDER BY book, title")
    fun observeByIds(ids: List<String>): Flow<List<TextEntity>>

    @Query("SELECT * FROM texts WHERE id = :id")
    fun observeById(id: String): Flow<TextEntity?>

    @Query("SELECT * FROM texts WHERE id = :id")
    suspend fun getById(id: String): TextEntity?

    @Query("SELECT * FROM texts WHERE title LIKE :q OR author LIKE :q")
    suspend fun search(q: String): List<TextEntity>

    @Query("SELECT COUNT(*) FROM texts")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(text: TextEntity)

    @Upsert
    suspend fun upsertAll(texts: List<TextEntity>)
}

@Dao
interface OccurrenceDao {
    @Query("SELECT DISTINCT textId FROM occurrences WHERE char = :char AND yixiang = :yixiang")
    fun observeTextIds(char: String, yixiang: String): Flow<List<String>>

    @Query("SELECT * FROM occurrences WHERE textId = :textId AND char = :char ORDER BY start")
    suspend fun occurrencesFor(textId: String, char: String): List<OccurrenceEntity>

    @Insert
    suspend fun insertAll(items: List<OccurrenceEntity>)
}

@Dao
interface LearningDao {
    @Query("SELECT * FROM learning_records WHERE cardId = :cardId")
    suspend fun getByCardId(cardId: String): LearningRecordEntity?

    @Query("SELECT * FROM learning_records WHERE nextReviewAt <= :now AND stage < 6 ORDER BY nextReviewAt")
    fun observeDue(now: Long): Flow<List<LearningRecordEntity>>

    @Query("SELECT COUNT(*) FROM learning_records")
    fun observeLearnedCount(): Flow<Int>

    @Upsert
    suspend fun upsert(record: LearningRecordEntity)
}

@Dao
interface WrongAnswerDao {
    @Query("SELECT * FROM wrong_answers ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WrongAnswerEntity>>

    @Insert
    suspend fun insert(item: WrongAnswerEntity): Long

    @Query("UPDATE wrong_answers SET practiced = 1 WHERE id = :id")
    suspend fun markPracticed(id: Long)

    @Query("DELETE FROM wrong_answers WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface StatsDao {
    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 30")
    fun observeAll(): Flow<List<DailyStatEntity>>

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getByDate(date: String): DailyStatEntity?

    @Upsert
    suspend fun upsert(stat: DailyStatEntity)
}
