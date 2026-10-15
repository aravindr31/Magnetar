package com.northlight.yts.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YtsResponse<T>(
    @Json(name = "status") val status: String,
    @Json(name = "status_message") val statusMessage: String,
    @Json(name = "data") val data: T
)

@JsonClass(generateAdapter = true)
data class YtsMovieListData(
    @Json(name = "movie_count") val movieCount: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "page_number") val pageNumber: Int,
    @Json(name = "movies") val movies: List<YtsMovie> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YtsMovieData(
    @Json(name = "movie") val movie: YtsMovie
)

@JsonClass(generateAdapter = true)
data class YtsMovie(
    @Json(name = "id") val id: Int,
    @Json(name = "url") val url: String,
    @Json(name = "imdb_code") val imdbCode: String,
    @Json(name = "title") val title: String,
    @Json(name = "title_long") val titleLong: String,
    @Json(name = "year") val year: Int,
    @Json(name = "rating") val rating: Float,
    @Json(name = "runtime") val runtime: Int,
    @Json(name = "genres") val genres: List<String> = emptyList(),
    @Json(name = "summary") val summary: String = "",
    @Json(name = "description_full") val descriptionFull: String = "",
    @Json(name = "language") val language: String,
    @Json(name = "mpa_rating") val mpaRating: String = "",
    @Json(name = "background_image") val backgroundImage: String,
    @Json(name = "background_image_original") val backgroundImageOriginal: String,
    @Json(name = "small_cover_image") val smallCoverImage: String,
    @Json(name = "medium_cover_image") val mediumCoverImage: String,
    @Json(name = "large_cover_image") val largeCoverImage: String,
    @Json(name = "torrents") val torrents: List<YtsTorrent> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YtsTorrent(
    @Json(name = "url") val url: String,
    @Json(name = "hash") val hash: String,
    @Json(name = "quality") val quality: String,       // "720p", "1080p", "2160p"
    @Json(name = "type") val type: String,             // "bluray", "web"
    @Json(name = "video_codec") val videoCodec: String = "",
    @Json(name = "bit_depth") val bitDepth: String = "",
    @Json(name = "audio_channels") val audioChannels: String = "",
    @Json(name = "seeds") val seeds: Int,
    @Json(name = "peers") val peers: Int,
    @Json(name = "size") val size: String,
    @Json(name = "size_bytes") val sizeBytes: Long,
    @Json(name = "date_uploaded") val dateUploaded: String,
    @Json(name = "date_uploaded_unix") val dateUploadedUnix: Long
) {
    /** Constructs magnet URI from the torrent hash */
    fun toMagnetLink(movieTitle: String): String {
        val encodedTitle = java.net.URLEncoder.encode(movieTitle, "UTF-8")
        return "magnet:?xt=urn:btih:$hash" +
                "&dn=$encodedTitle" +
                "&tr=udp://open.demonii.com:1337/announce" +
                "&tr=udp://tracker.openbittorrent.com:80" +
                "&tr=udp://tracker.coppersurfer.tk:6969" +
                "&tr=udp://glotorrents.pw:6969/announce" +
                "&tr=udp://tracker.opentrackr.org:1337/announce" +
                "&tr=udp://torrent.gresille.org:80/announce" +
                "&tr=udp://p4p.arenabg.com:1337" +
                "&tr=udp://tracker.leechers-paradise.org:6969"
    }
}

