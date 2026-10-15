package com.northlight.tmdb.di

import com.northlight.tmdb.api.TmdbApiService
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Injects the TMDB API key into every request as a query parameter.
 * Key is stored in local.properties and injected at build time via BuildConfig.
 */
class TmdbAuthInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("api_key", apiKey)
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}

@Module
@InstallIn(SingletonComponent::class)
object TmdbModule {

    private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"

    @Provides
    @Singleton
    @Named("tmdb")
    fun provideTmdbOkHttpClient(
        @Named("base") base: OkHttpClient,
        // API key injected via BuildConfig.TMDB_API_KEY — set in local.properties
        // For now using a placeholder; user must set their own key
    ): OkHttpClient = base.newBuilder()
        .addInterceptor(TmdbAuthInterceptor(com.northlight.tmdb.BuildConfig.TMDB_API_KEY))
        .build()

    @Provides
    @Singleton
    @Named("tmdb")
    fun provideTmdbRetrofit(
        @Named("tmdb") okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(TMDB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideTmdbApiService(@Named("tmdb") retrofit: Retrofit): TmdbApiService =
        retrofit.create(TmdbApiService::class.java)
}
