package com.example.screenshotmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.screenshotmanager.ui.ScreenshotApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.activity.enableEdgeToEdge()
        setContent {
            ScreenshotApp()
        }
    }
}
