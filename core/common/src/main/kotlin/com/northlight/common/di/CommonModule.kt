package com.northlight.common.di

import com.northlight.common.dispatcher.AppDispatchers
import com.northlight.common.dispatcher.DefaultAppDispatchers
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonModule {

    @Binds
    @Singleton
    abstract fun bindAppDispatchers(impl: DefaultAppDispatchers): AppDispatchers
}

