package com.example.screenshotmanager.di

import com.example.screenshotmanager.data.GeminiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGeminiService(): GeminiService {
        return GeminiService()
    }
}
