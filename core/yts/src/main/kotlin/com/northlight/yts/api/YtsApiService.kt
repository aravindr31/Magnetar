package com.northlight.yts.api

import com.northlight.yts.model.YtsMovieData
import com.northlight.yts.model.YtsMovieListData
import com.northlight.yts.model.YtsResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the YTS public API.
 * Base URL: https://movies-api.accel.li/api/v2/
 */
interface YtsApiService {

    /**
     * Get a paginated list of movies with optional filtering.
     */
    @GET("list_movies.json")
    suspend fun listMovies(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("quality") quality: String? = null,         // "720p", "1080p", "2160p"
        @Query("minimum_rating") minimumRating: Int? = null,
        @Query("query_term") queryTerm: String? = null,
        @Query("genre") genre: String? = null,
        @Query("sort_by") sortBy: String = "date_added",   // "title", "year", "rating", "seeds", "peers", "download_count", "like_count", "date_added"
        @Query("order_by") orderBy: String = "desc",
        @Query("with_rt_ratings") withRtRatings: Boolean = false
    ): YtsResponse<YtsMovieListData>

    /**
     * Get detailed information about a specific movie.
     */
    @GET("movie_details.json")
    suspend fun getMovieDetails(
        @Query("movie_id") movieId: Int,
        @Query("with_images") withImages: Boolean = true,
        @Query("with_cast") withCast: Boolean = true
    ): YtsResponse<YtsMovieData>

    /**
     * Get movie suggestions based on a movie ID.
     */
    @GET("movie_suggestions.json")
    suspend fun getMovieSuggestions(
        @Query("movie_id") movieId: Int
    ): YtsResponse<YtsMovieListData>
}

