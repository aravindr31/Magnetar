package com.northlight.feature.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.northlight.common.result.getOrNull
import com.northlight.common.result.onError
import com.northlight.common.result.onSuccess
import com.northlight.tmdb.repository.TmdbRepository
import com.northlight.yts.model.YtsTorrent
import com.northlight.yts.repository.YtsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ytsRepository: YtsRepository,
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    val movieId: Int = checkNotNull(savedStateHandle.get<String>("movieId")?.toIntOrNull()
        ?: savedStateHandle.get<Int>("movieId"))

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val movieResult = ytsRepository.getMovieDetails(movieId)

            movieResult.onSuccess { ytsMovie ->
                // Auto-select best quality torrent: highest seeds
                val bestTorrent = ytsMovie.torrents.maxByOrNull { it.seeds }

                _uiState.update {
                    it.copy(
                        ytsMovie = ytsMovie,
                        selectedTorrent = bestTorrent
                    )
                }

                // Asynchronously fetch TMDB details and suggestions
                val tmdbDeferred = async {
                    if (ytsMovie.imdbCode.isNotBlank()) {
                        tmdbRepository.getMovieByImdbId(ytsMovie.imdbCode)
                    } else null
                }

                val suggestionsDeferred = async {
                    ytsRepository.getMovieSuggestions(movieId)
                }

                val tmdbResult = tmdbDeferred.await()
                val suggestionsResult = suggestionsDeferred.await()

                val tmdbMovie = tmdbResult?.getOrNull()
                val suggestions = suggestionsResult.getOrNull() ?: emptyList()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tmdbMovie = tmdbMovie,
                        suggestions = suggestions
                    )
                }
            }.onError { _, msg ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = msg
                    )
                }
            }
        }
    }

    fun selectTorrent(torrent: YtsTorrent) {
        _uiState.update { it.copy(selectedTorrent = torrent) }
    }
}

