package com.northlight.feature.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.northlight.common.result.onError
import com.northlight.common.result.onSuccess
import com.northlight.yts.repository.YtsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val ytsRepository: YtsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        searchQueryFlow
            .debounce(400)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotBlank()) {
                    executeSearch(query)
                } else {
                    _uiState.update { it.copy(results = emptyList(), hasSearched = false, isLoading = false) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchQueryFlow.value = newQuery
    }

    fun executeSearch(query: String = _uiState.value.query) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, hasSearched = true) }
            val result = ytsRepository.searchMovies(query.trim())
            result.onSuccess { movies ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = movies,
                        error = null
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
}

