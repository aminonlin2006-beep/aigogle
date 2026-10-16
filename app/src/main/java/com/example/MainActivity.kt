package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BackupScreen
import com.example.ui.screens.FarmDetailScreen
import com.example.ui.screens.FarmsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PeopleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TagsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.AgriManagerTheme
import com.example.ui.viewmodel.AgriViewModel
import com.example.ui.viewmodel.AppScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AgriViewModel = viewModel()
            val deviceSettings by viewModel.deviceSettings.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            val isDark = deviceSettings?.theme == "dark"
            val isLargeFont = deviceSettings?.fontSize == "large"

            AgriManagerTheme(
                darkTheme = isDark,
                isLargeFont = isLargeFont
            ) {
                // Handle system back button for custom screen stack
                BackHandler(enabled = currentScreen !is AppScreen.Home) {
                    viewModel.navigateBack()
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    when (val screen = currentScreen) {
                        is AppScreen.Home -> HomeScreen(viewModel = viewModel)
                        is AppScreen.Tasks -> TasksScreen(viewModel = viewModel)
                        is AppScreen.Farms -> FarmsScreen(viewModel = viewModel)
                        is AppScreen.FarmDetail -> FarmDetailScreen(farmId = screen.farmId, viewModel = viewModel)
                        is AppScreen.People -> PeopleScreen(viewModel = viewModel)
                        is AppScreen.Tags -> TagsScreen(viewModel = viewModel)
                        is AppScreen.Settings -> SettingsScreen(viewModel = viewModel)
                        is AppScreen.Backup -> BackupScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
