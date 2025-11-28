package com.example.screenshotmanager.di

import android.content.Context
import androidx.room.Room
import com.example.screenshotmanager.data.AppDatabase
import com.example.screenshotmanager.data.ScreenshotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "screenshot_db"
        ).build()
    }

    @Provides
    fun provideScreenshotDao(database: AppDatabase): ScreenshotDao {
        return database.screenshotDao()
    }
}
