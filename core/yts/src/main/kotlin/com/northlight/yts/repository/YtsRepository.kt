package com.northlight.yts.repository

import com.northlight.common.dispatcher.AppDispatchers
import com.northlight.common.extensions.safeApiCall
import com.northlight.common.result.Result
import com.northlight.yts.api.YtsApiService
import com.northlight.yts.model.YtsMovie
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for all YTS movie data.
 * Single source of truth for movie listings, search, and details.
 */
@Singleton
class YtsRepository @Inject constructor(
    private val api: YtsApiService,
    private val dispatchers: AppDispatchers
) {
    /**
     * Fetches a paginated list of movies.
     * @param page 1-indexed page number
     * @param query Optional search term
     * @param genre Optional genre filter
     * @param quality Optional quality filter ("720p", "1080p", "2160p")
     * @param sortBy Sort criterion
     */
    suspend fun getMovies(
        page: Int = 1,
        query: String? = null,
        genre: String? = null,
        quality: String? = null,
        sortBy: String = "date_added"
    ): Result<List<YtsMovie>> = safeApiCall(dispatchers.io) {
        api.listMovies(
            page = page,
            queryTerm = query,
            genre = genre,
            quality = quality,
            sortBy = sortBy
        ).data.movies
    }

    /**
     * Fetches detailed info about a single movie including full cast + torrent list.
     */
    suspend fun getMovieDetails(movieId: Int): Result<YtsMovie> = safeApiCall(dispatchers.io) {
        api.getMovieDetails(movieId = movieId, withImages = true, withCast = true).data.movie
    }

    /**
     * Fetches similar movie suggestions.
     */
    suspend fun getMovieSuggestions(movieId: Int): Result<List<YtsMovie>> = safeApiCall(dispatchers.io) {
        api.getMovieSuggestions(movieId).data.movies
    }

    /**
     * Searches movies by title.
     */
    suspend fun searchMovies(query: String, page: Int = 1): Result<List<YtsMovie>> =
        getMovies(page = page, query = query)
}
