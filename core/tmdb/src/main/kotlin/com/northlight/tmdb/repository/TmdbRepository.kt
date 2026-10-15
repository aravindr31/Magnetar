package com.northlight.tmdb.repository

import com.northlight.common.dispatcher.AppDispatchers
import com.northlight.common.extensions.safeApiCall
import com.northlight.common.result.Result
import com.northlight.tmdb.api.TmdbApiService
import com.northlight.tmdb.model.TmdbMovie
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for all TMDB movie data.
 * Primary use: enrich YTS movie data with TMDB's richer metadata
 * (cast, director, backdrop, tagline, etc.) using IMDB IDs from YTS.
 */
@Singleton
class TmdbRepository @Inject constructor(
    private val api: TmdbApiService,
    private val dispatchers: AppDispatchers
) {
    /**
     * Fetches full TMDB movie details using IMDB ID (provided by YTS).
     * This is the primary entry point — YTS gives us the IMDB code,
     * we use it to pull richer data from TMDB.
     */
    suspend fun getMovieByImdbId(imdbId: String): Result<TmdbMovie?> = safeApiCall(dispatchers.io) {
        api.findByImdbId(imdbId).movieResults.firstOrNull()?.let { partial ->
            // Fetch full details with credits appended
            api.getMovieById(partial.id)
        }
    }

    /**
     * Fetches full TMDB movie details by TMDB ID directly.
     */
    suspend fun getMovieById(tmdbId: Int): Result<TmdbMovie> = safeApiCall(dispatchers.io) {
        api.getMovieById(tmdbId)
    }
}

