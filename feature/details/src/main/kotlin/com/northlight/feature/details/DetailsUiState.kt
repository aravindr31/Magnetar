package com.northlight.feature.details

import com.northlight.tmdb.model.TmdbMovie
import com.northlight.yts.model.YtsMovie
import com.northlight.yts.model.YtsTorrent

data class DetailsUiState(
    val isLoading: Boolean = true,
    val ytsMovie: YtsMovie? = null,
    val tmdbMovie: TmdbMovie? = null,
    val suggestions: List<YtsMovie> = emptyList(),
    val selectedTorrent: YtsTorrent? = null,
    val error: String? = null
)

