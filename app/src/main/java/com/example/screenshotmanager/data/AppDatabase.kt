package com.example.screenshotmanager.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScreenshotEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun screenshotDao(): ScreenshotDao
}
