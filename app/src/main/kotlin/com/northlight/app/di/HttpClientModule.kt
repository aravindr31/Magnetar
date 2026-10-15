package com.northlight.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provides OkHttpClient instances for streaming providers that need direct HTTP access
 * (Seedr, Real-Debrid) without the full Retrofit stack.
 */
@Module
@InstallIn(SingletonComponent::class)
object HttpClientModule {

    @Provides
    @Singleton
    @Named("streaming")
    fun provideStreamingOkHttpClient(@Named("base") base: OkHttpClient): OkHttpClient =
        base.newBuilder()
            .followRedirects(true)
            .build()
}

