package com.example.screenshotmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screenshotmanager.data.ScreenshotEntity
import com.example.screenshotmanager.data.ScreenshotRepository
import com.example.screenshotmanager.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScreenshotViewModel @Inject constructor(
    private val repository: ScreenshotRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val apiKey: StateFlow<String> = settingsRepository.apiKey

    val screenshots: StateFlow<List<ScreenshotEntity>> = _searchQuery
        .combine(repository.allScreenshots) { query, list ->
            if (query.isBlank()) {
                list
            } else {
                list.filter {
                    (it.summary?.contains(query, ignoreCase = true) == true) ||
                    (it.classification?.contains(query, ignoreCase = true) == true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setApiKey(key: String) {
        settingsRepository.saveApiKey(key)
    }

    fun refresh() {
        viewModelScope.launch {
            repository.processNewScreenshots()
        }
    }
}
