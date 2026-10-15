package com.northlight.tmdb.api

import com.northlight.tmdb.model.TmdbMovie
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for TMDB API v3.
 * Base URL: https://api.themoviedb.org/3/
 * API key passed via [TmdbAuthInterceptor].
 */
interface TmdbApiService {

    /**
     * Fetch movie details by TMDB ID. Append credits in one call.
     */
    @GET("movie/{movie_id}")
    suspend fun getMovieById(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits"
    ): TmdbMovie

    /**
     * Find movie by external ID (e.g., IMDB ID from YTS).
     */
    @GET("find/{external_id}")
    suspend fun findByImdbId(
        @Path("external_id") imdbId: String,
        @Query("external_source") externalSource: String = "imdb_id"
    ): TmdbFindResponse
}

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbFindResponse(
    @Json(name = "movie_results") val movieResults: List<TmdbMovie> = emptyList()
)
