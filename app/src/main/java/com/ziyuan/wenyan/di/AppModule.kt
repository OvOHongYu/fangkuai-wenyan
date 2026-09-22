package com.ziyuan.wenyan.di

import android.content.Context
import androidx.room.Room
import com.ziyuan.wenyan.data.local.AppDatabase
import com.ziyuan.wenyan.data.local.dao.CardDao
import com.ziyuan.wenyan.data.local.dao.LearningDao
import com.ziyuan.wenyan.data.local.dao.OccurrenceDao
import com.ziyuan.wenyan.data.local.dao.StatsDao
import com.ziyuan.wenyan.data.local.dao.TextDao
import com.ziyuan.wenyan.data.local.dao.WrongAnswerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wenyan.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    fun provideCardDao(db: AppDatabase): CardDao = db.cardDao()

    @Provides
    fun provideTextDao(db: AppDatabase): TextDao = db.textDao()

    @Provides
    fun provideOccurrenceDao(db: AppDatabase): OccurrenceDao = db.occurrenceDao()

    @Provides
    fun provideLearningDao(db: AppDatabase): LearningDao = db.learningDao()

    @Provides
    fun provideWrongAnswerDao(db: AppDatabase): WrongAnswerDao = db.wrongAnswerDao()

    @Provides
    fun provideStatsDao(db: AppDatabase): StatsDao = db.statsDao()
}
