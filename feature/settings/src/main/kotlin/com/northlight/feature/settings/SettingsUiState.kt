package com.northlight.feature.settings

import com.northlight.streaming.StreamingBackend

data class SettingsUiState(
    val selectedBackend: StreamingBackend = StreamingBackend.TORRENT,
    val seedrConfigured: Boolean = false,
    val realDebridConfigured: Boolean = false,
    val showSeedrLogin: Boolean = false,
    val seedrUsernameInput: String = "",
    val seedrPasswordInput: String = "",
    val realDebridTokenInput: String = "",
    val isSaving: Boolean = false,
    val message: String? = null
)

