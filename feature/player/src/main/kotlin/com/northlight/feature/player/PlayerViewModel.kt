package com.northlight.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.northlight.common.result.onError
import com.northlight.common.result.onSuccess
import com.northlight.streaming.StreamResult
import com.northlight.streaming.StreamingProvider
import com.northlight.yts.repository.YtsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ytsRepository: YtsRepository,
    private val streamingProvider: StreamingProvider
) : ViewModel() {

    val movieId: Int = checkNotNull(savedStateHandle.get<String>("movieId")?.toIntOrNull()
        ?: savedStateHandle.get<Int>("movieId"))
    val torrentHash: String = checkNotNull(savedStateHandle.get<String>("torrentHash"))

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var activeStreamResult: StreamResult? = null

    init {
        startStream()
    }

    fun startStream() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isResolving = true,
                    error = null,
                    progressMessage = "Fetching movie information..."
                )
            }

            // 1. Fetch movie details to obtain movie title and torrent info
            val movieResult = ytsRepository.getMovieDetails(movieId)

            movieResult.onSuccess { movie ->
                val torrent = movie.torrents.firstOrNull { it.hash.equals(torrentHash, ignoreCase = true) }
                    ?: movie.torrents.firstOrNull()

                if (torrent == null) {
                    _uiState.update {
                        it.copy(
                            isResolving = false,
                            error = "Requested torrent was not found."
                        )
                    }
                    return@launch
                }

                val magnet = torrent.toMagnetLink(movie.title)
                _uiState.update {
                    it.copy(
                        movieTitle = movie.title,
                        progressMessage = "Connecting to swarm and buffering torrent pieces..."
                    )
                }

                // 2. Resolve stream via the streaming provider (TorrentStreamProvider)
                val streamRes = streamingProvider.resolveStream(magnet)

                streamRes.onSuccess { result ->
                    activeStreamResult = result
                    _uiState.update {
                        it.copy(
                            isResolving = false,
                            streamResult = result,
                            streamUrl = result.url,
                            error = null
                        )
                    }
                }.onError { _, errorMsg ->
                    _uiState.update {
                        it.copy(
                            isResolving = false,
                            error = errorMsg
                        )
                    }
                }
            }.onError { _, errorMsg ->
                _uiState.update {
                    it.copy(
                        isResolving = false,
                        error = errorMsg
                    )
                }
            }
        }
    }

    fun setVlcAvailable(available: Boolean) {
        _uiState.update { it.copy(isVlcAvailable = available) }
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    fun cleanup() {
        activeStreamResult?.let { result ->
            viewModelScope.launch {
                streamingProvider.cleanup(result)
            }
        }
    }
}

