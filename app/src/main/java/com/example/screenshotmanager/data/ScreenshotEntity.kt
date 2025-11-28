package com.example.screenshotmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey val uri: String,
    val path: String,
    val timestamp: Long,
    val summary: String? = null,
    val classification: String? = null,
    val isProcessed: Boolean = false
)
