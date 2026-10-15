package com.northlight.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val movieId: Int,
    val title: String,
    val posterUrl: String,
    val backdropUrl: String?,
    val torrentHash: String,
    val streamUrl: String?,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val lastWatchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteMovieEntity(
    @PrimaryKey
    val movieId: Int,
    val title: String,
    val year: Int,
    val posterUrl: String,
    val rating: Float,
    val addedAt: Long = System.currentTimeMillis()
)

