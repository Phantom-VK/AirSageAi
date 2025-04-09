package com.vikram.airsageai.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.vikram.airsageai.ui.components.AppBottomBar
import com.vikram.airsageai.viewmodels.ScreenViewModel

@Composable
fun MainScaffoldScreen(){
    val navController = rememberNavController()
    val screenViewModel: ScreenViewModel = viewModel()

    Scaffold(
        bottomBar = {
            AppBottomBar()
        }
    ) { paddingValues ->

        when(screenViewModel.currentScreen){
            Screen.Home -> HomeScreen(navController, paddingValues)
            Screen.Analytics -> AnalyticsScreen(navController, paddingValues)
            Screen.Info -> InfoScreen(navController, paddingValues)
            Screen.Settings -> SettingsScreen(navController, paddingValues)

            else -> HomeScreen(navController, paddingValues)
        }

    }
}





