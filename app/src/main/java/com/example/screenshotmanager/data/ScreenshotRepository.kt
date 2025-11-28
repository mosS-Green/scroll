package com.example.screenshotmanager.data

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenshotDao: ScreenshotDao,
    private val geminiService: GeminiService
) {

    val allScreenshots: Flow<List<ScreenshotEntity>> = screenshotDao.getAllScreenshots()

    fun searchScreenshots(query: String): Flow<List<ScreenshotEntity>> = screenshotDao.searchScreenshots(query)

    suspend fun processNewScreenshots() {
        withContext(Dispatchers.IO) {
            // Logic to find new screenshots
            // For simplicity, let's say we check for screenshots added after the last known timestamp in DB
            // If DB is empty, we might want to ignore existing ones as per requirement "ignores old files"
            // But usually "ignores old files" means don't backfill everything. 
            // We can store a preference for "last sync time".
            
            val sharedPrefs = context.getSharedPreferences("screenshot_prefs", Context.MODE_PRIVATE)
            val lastSyncTime = sharedPrefs.getLong("last_sync_time", System.currentTimeMillis() / 1000)

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATA
            )

            val selection = "${MediaStore.Images.Media.DATE_ADDED} > ? AND ${MediaStore.Images.Media.DISPLAY_NAME} LIKE 'Screenshot%'"
            val selectionArgs = arrayOf(lastSyncTime.toString())
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val path = cursor.getString(dataColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    // Check if already exists (double check)
                    if (screenshotDao.getScreenshotByUri(contentUri.toString()) == null) {
                        val entity = ScreenshotEntity(
                            uri = contentUri.toString(),
                            path = path,
                            timestamp = dateAdded * 1000
                        )
                        screenshotDao.insertScreenshot(entity)
                        analyzeScreenshot(entity)
                    }
                }
            }

            // Update last sync time
            sharedPrefs.edit().putLong("last_sync_time", System.currentTimeMillis() / 1000).apply()
        }
    }

    private suspend fun analyzeScreenshot(screenshot: ScreenshotEntity) {
        try {
            val uri = Uri.parse(screenshot.uri)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val analysis = geminiService.analyzeScreenshot(bitmap)
                    screenshotDao.updateScreenshotAnalysis(
                        uri = screenshot.uri,
                        summary = analysis.summary,
                        classification = analysis.classification
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun manualAdd(uri: Uri) {
         // TODO: Implement manual add logic
    }
}
