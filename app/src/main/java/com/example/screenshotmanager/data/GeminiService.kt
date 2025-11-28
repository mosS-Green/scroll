package com.example.screenshotmanager.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import javax.inject.Inject

class GeminiService @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun analyzeScreenshot(bitmap: Bitmap): ScreenshotAnalysis {
        val apiKey = settingsRepository.apiKey.value
        val modelName = settingsRepository.modelName.value
        
        if (apiKey.isBlank()) {
            return ScreenshotAnalysis("API Key missing", "Error")
        }

        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey
        )

        val prompt = "Analyze this screenshot. Provide a short summary (max 2 sentences) and a classification category (e.g., Social Media, Finance, News, Code, Chat, Other). Return the result in the format: 'Summary: [summary] | Category: [category]'"
        
        try {
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            val text = response.text ?: return ScreenshotAnalysis("No summary available", "Unknown")
            
            // Simple parsing logic (can be improved)
            val parts = text.split("|")
            val summary = parts.getOrNull(0)?.substringAfter("Summary:")?.trim() ?: "No summary"
            val category = parts.getOrNull(1)?.substringAfter("Category:")?.trim() ?: "Unknown"

            return ScreenshotAnalysis(summary, category)
        } catch (e: Exception) {
            return ScreenshotAnalysis("Error: ${e.message}", "Error")
        }
    }
}

data class ScreenshotAnalysis(
    val summary: String,
    val classification: String
)
