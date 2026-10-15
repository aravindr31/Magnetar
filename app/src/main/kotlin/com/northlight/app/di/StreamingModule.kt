package com.northlight.app.di

import com.northlight.streaming.StreamingBackend
import com.northlight.streaming.StreamingConfig
import com.northlight.streaming.StreamingProvider
import com.northlight.streaming.realdebrid.RealDebridProvider
import com.northlight.streaming.seedr.SeedrProvider
import com.northlight.streaming.torrent.TorrentStreamProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

/**
 * Hilt module that wires up the active [StreamingProvider] based on the
 * user's persisted [StreamingBackend] choice in [StreamingConfig].
 */
@Module
@InstallIn(SingletonComponent::class)
object StreamingModule {

    @Provides
    @Singleton
    fun provideStreamingProvider(
        config: StreamingConfig,
        torrentProvider: TorrentStreamProvider,
        seedrProvider: SeedrProvider,
        realDebridProvider: RealDebridProvider
    ): StreamingProvider {
        val backend = runBlocking { config.selectedBackend.first() }
        return when (backend) {
            StreamingBackend.TORRENT     -> torrentProvider
            StreamingBackend.SEEDR       -> seedrProvider
            StreamingBackend.REAL_DEBRID -> realDebridProvider
        }
    }
}

