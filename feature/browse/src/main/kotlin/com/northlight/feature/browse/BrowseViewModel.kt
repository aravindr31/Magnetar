package com.northlight.feature.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.northlight.common.result.onError
import com.northlight.common.result.onSuccess
import com.northlight.yts.repository.YtsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val ytsRepository: YtsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        loadMovies(page = 1)
    }

    fun loadMovies(page: Int = 1) {
        viewModelScope.launch {
            if (page == 1) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            val currentState = _uiState.value
            val result = ytsRepository.getMovies(
                page = page,
                genre = currentState.selectedGenre,
                quality = currentState.selectedQuality,
                sortBy = currentState.sortBy
            )

            result.onSuccess { movies ->
                _uiState.update { state ->
                    val combined = if (page == 1) movies else state.movies + movies
                    state.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        movies = combined,
                        currentPage = page,
                        hasMorePages = movies.isNotEmpty()
                    )
                }
            }.onError { _, message ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = if (page == 1) message else null
                    )
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.isLoading && !state.isLoadingMore && state.hasMorePages) {
            loadMovies(page = state.currentPage + 1)
        }
    }

    fun setGenre(genre: String?) {
        if (_uiState.value.selectedGenre != genre) {
            _uiState.update { it.copy(selectedGenre = genre) }
            loadMovies(page = 1)
        }
    }

    fun setQuality(quality: String?) {
        if (_uiState.value.selectedQuality != quality) {
            _uiState.update { it.copy(selectedQuality = quality) }
            loadMovies(page = 1)
        }
    }

    fun setSortBy(sortBy: String) {
        if (_uiState.value.sortBy != sortBy) {
            _uiState.update { it.copy(sortBy = sortBy) }
            loadMovies(page = 1)
        }
    }
}

