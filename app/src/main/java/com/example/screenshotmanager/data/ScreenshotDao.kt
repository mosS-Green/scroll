package com.example.screenshotmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshots ORDER BY timestamp DESC")
    fun getAllScreenshots(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE summary LIKE '%' || :query || '%' OR classification LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchScreenshots(query: String): Flow<List<ScreenshotEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScreenshot(screenshot: ScreenshotEntity)

    @Query("UPDATE screenshots SET summary = :summary, classification = :classification, isProcessed = 1 WHERE uri = :uri")
    suspend fun updateScreenshotAnalysis(uri: String, summary: String, classification: String)

    @Query("SELECT * FROM screenshots WHERE uri = :uri")
    suspend fun getScreenshotByUri(uri: String): ScreenshotEntity?
    
    @Query("SELECT COUNT(*) FROM screenshots")
    suspend fun getScreenshotCount(): Int
}
