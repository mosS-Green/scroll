package com.example.screenshotmanager.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import javax.inject.Inject

class GeminiService @Inject constructor() {
    // TODO: Replace with actual API Key or inject it
    private val apiKey = "YOUR_API_KEY" 
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun analyzeScreenshot(bitmap: Bitmap): ScreenshotAnalysis {
        val prompt = "Analyze this screenshot. Provide a short summary (max 2 sentences) and a classification category (e.g., Social Media, Finance, News, Code, Chat, Other). Return the result in the format: 'Summary: [summary] | Category: [category]'"
        
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
    }
}

data class ScreenshotAnalysis(
    val summary: String,
    val classification: String
)
