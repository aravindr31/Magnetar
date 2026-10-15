package com.northlight.feature.browse

import com.northlight.yts.model.YtsMovie

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<YtsMovie> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false
)

