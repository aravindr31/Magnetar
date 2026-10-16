package com.northlight.yts.di

import com.northlight.yts.api.YtsApiService
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object YtsModule {

    private const val YTS_BASE_URL = "https://movies-api.accel.li/api/v2/"

    @Provides
    @Singleton
    @Named("yts")
    fun provideYtsRetrofit(
        @Named("base") okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(YTS_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideYtsApiService(@Named("yts") retrofit: Retrofit): YtsApiService =
        retrofit.create(YtsApiService::class.java)
}

