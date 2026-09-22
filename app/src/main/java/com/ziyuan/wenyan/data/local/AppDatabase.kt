package com.ziyuan.wenyan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ziyuan.wenyan.data.local.dao.CardDao
import com.ziyuan.wenyan.data.local.dao.LearningDao
import com.ziyuan.wenyan.data.local.dao.OccurrenceDao
import com.ziyuan.wenyan.data.local.dao.StatsDao
import com.ziyuan.wenyan.data.local.dao.TextDao
import com.ziyuan.wenyan.data.local.dao.WrongAnswerDao
import com.ziyuan.wenyan.data.local.entity.CardEntity
import com.ziyuan.wenyan.data.local.entity.DailyStatEntity
import com.ziyuan.wenyan.data.local.entity.LearningRecordEntity
import com.ziyuan.wenyan.data.local.entity.OccurrenceEntity
import com.ziyuan.wenyan.data.local.entity.TextEntity
import com.ziyuan.wenyan.data.local.entity.WrongAnswerEntity

@Database(
    entities = [
        CardEntity::class,
        TextEntity::class,
        OccurrenceEntity::class,
        LearningRecordEntity::class,
        WrongAnswerEntity::class,
        DailyStatEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun textDao(): TextDao
    abstract fun occurrenceDao(): OccurrenceDao
    abstract fun learningDao(): LearningDao
    abstract fun wrongAnswerDao(): WrongAnswerDao
    abstract fun statsDao(): StatsDao
}
