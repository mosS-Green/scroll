package com.example.screenshotmanager.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    private val _apiKey = MutableStateFlow(prefs.getString("api_key", "") ?: "")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _modelName = MutableStateFlow(prefs.getString("model_name", "gemini-1.5-flash-latest") ?: "gemini-1.5-flash-latest")
    val modelName: StateFlow<String> = _modelName.asStateFlow()

    fun saveApiKey(key: String) {
        prefs.edit().putString("api_key", key).apply()
        _apiKey.value = key
    }

    fun saveModelName(model: String) {
        prefs.edit().putString("model_name", model).apply()
        _modelName.value = model
    }

    fun hasApiKey(): Boolean {
        return _apiKey.value.isNotBlank()
    }
}
