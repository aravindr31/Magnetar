package com.northlight.feature.player

import com.northlight.streaming.StreamResult

data class PlayerUiState(
    val isResolving: Boolean = true,
    val streamResult: StreamResult? = null,
    val streamUrl: String? = null,
    val movieTitle: String = "",
    val error: String? = null,
    val progressMessage: String = "Initializing local torrent stream...",
    val isVlcAvailable: Boolean = false
)

