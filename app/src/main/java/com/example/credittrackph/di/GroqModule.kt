package com.example.credittrackph.di

import com.example.credittrackph.data.network.GroqApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GroqModule {
    @Provides
    @Singleton
    fun provideGroqApiService(retrofit: Retrofit): GroqApiService =
        retrofit.create(GroqApiService::class.java)
}
