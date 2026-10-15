package com.northlight.streaming

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists and exposes the user's selected [StreamingBackend].
 * Backed by Jetpack DataStore.
 */
@Singleton
class StreamingConfig @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_BACKEND = stringPreferencesKey("streaming_backend")
        val DEFAULT_BACKEND = StreamingBackend.TORRENT
    }

    val selectedBackend: Flow<StreamingBackend> = dataStore.data.map { prefs ->
        prefs[KEY_BACKEND]?.let { name ->
            runCatching { StreamingBackend.valueOf(name) }.getOrNull()
        } ?: DEFAULT_BACKEND
    }

    suspend fun setBackend(backend: StreamingBackend) {
        dataStore.edit { prefs ->
            prefs[KEY_BACKEND] = backend.name
        }
    }
}
