package com.northlight.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.northlight.core.database.dao.FavoriteMovieDao
import com.northlight.core.database.dao.WatchHistoryDao
import com.northlight.core.database.entity.FavoriteMovieEntity
import com.northlight.core.database.entity.WatchHistoryEntity

@Database(
    entities = [
        WatchHistoryEntity::class,
        FavoriteMovieEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NorthLightDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun favoriteMovieDao(): FavoriteMovieDao
}

