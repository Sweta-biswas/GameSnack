package com.swetabiswas.gamesnack.di

import android.content.Context
import com.swetabiswas.gamesnack.data.local.dao.BadgeDao
import com.swetabiswas.gamesnack.data.local.dao.GameScoreDao
import com.swetabiswas.gamesnack.data.local.dao.PlayerProfileDao
import com.swetabiswas.gamesnack.data.local.db.GameSnackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GameSnackDatabase =
        GameSnackDatabase.getDatabase(context)

    @Provides
    fun providePlayerProfileDao(db: GameSnackDatabase): PlayerProfileDao =
        db.playerProfileDao()

    @Provides
    fun provideGameScoreDao(db: GameSnackDatabase): GameScoreDao =
        db.gameScoreDao()

    @Provides
    fun provideBadgeDao(db: GameSnackDatabase): BadgeDao =
        db.badgeDao()
}
