package com.ziyuan.wenyan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val type: String,          // 实词 / 虚词 / 对比
    val char: String?,
    val pinyin: String?,
    val title: String?,
    val sortKey: String,       // 排序键
    val dataJson: String,      // 卡片完整 JSON
    val favorite: Boolean = false,
    val learned: Boolean = false
)

@Entity(tableName = "texts")
data class TextEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val dynasty: String,
    val book: String,
    val genre: String,
    val dataJson: String
)

@Entity(tableName = "occurrences")
data class OccurrenceEntity(
    val textId: String,
    val char: String,
    val yixiang: String,
    val start: Int,
    @PrimaryKey(autoGenerate = true) val uid: Long = 0
)

@Entity(tableName = "learning_records")
data class LearningRecordEntity(
    @PrimaryKey val cardId: String,
    val stage: Int = 0,
    val learnedAt: Long,
    val nextReviewAt: Long,
    val reviewCount: Int = 0
)

@Entity(tableName = "wrong_answers")
data class WrongAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: String?,
    val question: String,
    val options: String,       // JSON 数组字符串
    val userAnswer: String,
    val correctAnswer: String,
    val analysis: String,
    val sentence: String = "",
    val source: String = "",
    val createdAt: Long,
    val practiced: Boolean = false
)

@Entity(tableName = "daily_stats")
data class DailyStatEntity(
    @PrimaryKey val date: String,   // yyyy-MM-dd
    val cardsLearned: Int = 0,
    val reviewCount: Int = 0,
    val quizCount: Int = 0,
    val quizCorrect: Int = 0,
    val studySeconds: Int = 0
)
