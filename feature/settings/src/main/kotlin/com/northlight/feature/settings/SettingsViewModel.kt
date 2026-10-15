package com.northlight.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.northlight.common.result.onError
import com.northlight.common.result.onSuccess
import com.northlight.streaming.StreamingBackend
import com.northlight.streaming.StreamingConfig
import com.northlight.streaming.realdebrid.RealDebridProvider
import com.northlight.streaming.seedr.SeedrProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val config: StreamingConfig,
    private val seedrProvider: SeedrProvider,
    private val realDebridProvider: RealDebridProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        config.selectedBackend
            .onEach { backend ->
                _uiState.update {
                    it.copy(
                        selectedBackend = backend,
                        seedrConfigured = seedrProvider.isConfigured(),
                        realDebridConfigured = realDebridProvider.isConfigured()
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectBackend(backend: StreamingBackend) {
        viewModelScope.launch {
            config.setBackend(backend)
            _uiState.update {
                it.copy(
                    selectedBackend = backend,
                    message = "Switched to ${backend.displayName}"
                )
            }
        }
    }

    fun onSeedrUsernameChange(username: String) {
        _uiState.update { it.copy(seedrUsernameInput = username) }
    }

    fun onSeedrPasswordChange(password: String) {
        _uiState.update { it.copy(seedrPasswordInput = password) }
    }

    fun onRealDebridTokenChange(token: String) {
        _uiState.update { it.copy(realDebridTokenInput = token) }
    }

    fun saveRealDebridToken() {
        val token = _uiState.value.realDebridTokenInput.trim()
        if (token.isNotEmpty()) {
            realDebridProvider.setApiToken(token)
            _uiState.update {
                it.copy(
                    realDebridConfigured = true,
                    realDebridTokenInput = "",
                    message = "Real-Debrid API token saved."
                )
            }
        }
    }

    fun loginSeedr() {
        val username = _uiState.value.seedrUsernameInput.trim()
        val password = _uiState.value.seedrPasswordInput.trim()
        if (username.isEmpty() || password.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val res = seedrProvider.authenticate(username, password)
            res.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        seedrConfigured = true,
                        seedrUsernameInput = "",
                        seedrPasswordInput = "",
                        message = "Logged in to Seedr successfully."
                    )
                }
            }.onError { _, errorMsg ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = "Seedr login failed: $errorMsg"
                    )
                }
            }
        }
    }
}
