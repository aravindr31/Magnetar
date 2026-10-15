package com.northlight.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.northlight.core.database.entity.FavoriteMovieEntity
import com.northlight.core.database.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE movieId = :movieId")
    suspend fun getHistory(movieId: Int): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE movieId = :movieId")
    suspend fun deleteHistory(movieId: Int)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()
}

@Dao
interface FavoriteMovieDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteMovieEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE movieId = :movieId)")
    fun isFavorite(movieId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteMovieEntity)

    @Query("DELETE FROM favorites WHERE movieId = :movieId")
    suspend fun removeFavorite(movieId: Int)
}

