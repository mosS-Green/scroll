package com.example.screenshotmanager.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.screenshotmanager.ui.theme.ScreenshotManagerTheme

@Composable
fun ScreenshotApp() {
    ScreenshotManagerTheme {
        val context = LocalContext.current
        var hasPermission by remember {
            mutableStateOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                } else {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
            )
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasPermission = isGranted
        }

        LaunchedEffect(Unit) {
            if (!hasPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            if (hasPermission) {
                val viewModel: ScreenshotViewModel = viewModel()
                val apiKey by viewModel.apiKey.collectAsState()
                val navController = rememberNavController()
                
                if (apiKey.isBlank()) {
                    ApiKeyDialog(onApiKeyEntered = viewModel::setApiKey)
                } else {
                    LaunchedEffect(Unit) {
                        viewModel.refresh()
                    }
                    
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onSettingsClick = { navController.navigate("settings") },
                                onScreenshotClick = { screenshot ->
                                    // Encode URI to pass as argument
                                    val encodedUri = java.net.URLEncoder.encode(screenshot.uri, "UTF-8")
                                    navController.navigate("detail/$encodedUri")
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "detail/{uri}",
                            arguments = listOf(navArgument("uri") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val uri = backStackEntry.arguments?.getString("uri")
                            val decodedUri = java.net.URLDecoder.decode(uri, "UTF-8")
                            val screenshot = viewModel.getScreenshot(decodedUri)
                            
                            if (screenshot != null) {
                                DetailScreen(
                                    screenshot = screenshot,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }) {
                        Text("Grant Storage Permission")
                    }
                }
            }
        }
    }
}

@Composable
fun ApiKeyDialog(onApiKeyEntered: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {},
        title = { Text("Enter Gemini API Key") },
        text = {
            androidx.compose.material3.TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("API Key") }
            )
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (text.isNotBlank()) {
                        onApiKeyEntered(text)
                    }
                }
            ) {
                Text("Save")
            }
        }
    )
}
