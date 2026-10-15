package com.northlight.streaming

import com.northlight.common.result.Result

/**
 * Represents a resolved stream ready for playback.
 */
data class StreamResult(
    val url: String,
    val title: String,
    val headers: Map<String, String> = emptyMap()
)

/**
 * The core abstraction for all streaming backends.
 * Implementations: TorrentStreamProvider, RealDebridProvider, SeedrProvider.
 *
 * To switch providers, simply bind a different implementation in Hilt DI.
 */
interface StreamingProvider {
    /** Human-readable provider name shown in settings */
    val name: String

    /** Whether the provider has been configured by the user (credentials set, etc.) */
    fun isConfigured(): Boolean

    /**
     * Given a magnet link, resolves a direct streamable HTTP URL.
     * May involve uploading to cloud, waiting for torrent fetch, etc.
     *
     * @param magnetLink The magnet URI from YTS
     * @return [com.northlight.common.result.Result] wrapping a [StreamResult]
     */
    suspend fun resolveStream(magnetLink: String): com.northlight.common.result.Result<StreamResult>

    /**
     * Optional: Called after playback ends to clean up (e.g., delete from cloud storage).
     */
    suspend fun cleanup(streamResult: StreamResult) {}
}

