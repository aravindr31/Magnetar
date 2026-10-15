package com.northlight.feature.browse

import com.northlight.yts.model.YtsMovie

data class BrowseUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val movies: List<YtsMovie> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val selectedGenre: String? = null,
    val selectedQuality: String? = null,
    val sortBy: String = "date_added"
)

