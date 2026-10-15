package com.northlight.tmdb.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbMovie(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "overview") val overview: String,
    @Json(name = "tagline") val tagline: String = "",
    @Json(name = "status") val status: String = "",
    @Json(name = "release_date") val releaseDate: String = "",
    @Json(name = "runtime") val runtime: Int = 0,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "vote_count") val voteCount: Int,
    @Json(name = "popularity") val popularity: Double,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "genres") val genres: List<TmdbGenre> = emptyList(),
    @Json(name = "credits") val credits: TmdbCredits? = null,
    @Json(name = "imdb_id") val imdbId: String? = null,
    @Json(name = "homepage") val homepage: String? = null,
    @Json(name = "budget") val budget: Long = 0,
    @Json(name = "revenue") val revenue: Long = 0
) {
    fun posterUrl(size: String = "w500") =
        posterPath?.let { "https://image.tmdb.org/t/p/$size$it" }

    fun backdropUrl(size: String = "w1280") =
        backdropPath?.let { "https://image.tmdb.org/t/p/$size$it" }
}

@JsonClass(generateAdapter = true)
data class TmdbGenre(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class TmdbCredits(
    @Json(name = "cast") val cast: List<TmdbCast> = emptyList(),
    @Json(name = "crew") val crew: List<TmdbCrew> = emptyList()
) {
    fun director() = crew.firstOrNull { it.job == "Director" }
}

@JsonClass(generateAdapter = true)
data class TmdbCast(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "character") val character: String,
    @Json(name = "profile_path") val profilePath: String? = null,
    @Json(name = "order") val order: Int
) {
    fun profileUrl(size: String = "w185") =
        profilePath?.let { "https://image.tmdb.org/t/p/$size$it" }
}

@JsonClass(generateAdapter = true)
data class TmdbCrew(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "job") val job: String,
    @Json(name = "department") val department: String
)

