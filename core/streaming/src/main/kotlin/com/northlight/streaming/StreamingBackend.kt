package com.northlight.streaming

/**
 * Supported streaming backends.
 * Stored in DataStore to persist user's selection across sessions.
 */
enum class StreamingBackend(val displayName: String) {
    TORRENT("Local Torrent (Built-in)"),
    REAL_DEBRID("Real-Debrid"),
    SEEDR("Seedr")
}
