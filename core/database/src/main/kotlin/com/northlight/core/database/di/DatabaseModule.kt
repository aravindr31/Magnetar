package com.northlight.core.database.di

import android.content.Context
import androidx.room.Room
import com.northlight.core.database.NorthLightDatabase
import com.northlight.core.database.dao.FavoriteMovieDao
import com.northlight.core.database.dao.WatchHistoryDao
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
    fun provideDatabase(@ApplicationContext context: Context): NorthLightDatabase =
        Room.databaseBuilder(
            context,
            NorthLightDatabase::class.java,
            "northlight.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideWatchHistoryDao(database: NorthLightDatabase): WatchHistoryDao =
        database.watchHistoryDao()

    @Provides
    fun provideFavoriteMovieDao(database: NorthLightDatabase): FavoriteMovieDao =
        database.favoriteMovieDao()
}

