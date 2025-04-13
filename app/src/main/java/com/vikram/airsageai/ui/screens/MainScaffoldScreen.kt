package com.vikram.airsageai.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.vikram.airsageai.ui.components.AppBottomBar
import com.vikram.airsageai.utils.GasReading
import com.vikram.airsageai.viewmodels.ScreenViewModel

@Composable
fun MainScaffoldScreen(
    latestReading: GasReading?,
    aqiValues: Map<String, Int>?,
    overallAQI: Int?
){
    val navController = rememberNavController()

    val screenViewModel: ScreenViewModel = viewModel()






    var themeColor = when (overallAQI) {
        in 0..50 -> Color(0xFF96D9F3)
        in 51..100 -> Color(0xFFFCEFCC)
        else -> Color(0xFFFFC2A5)
    }



    Scaffold(
        bottomBar = {
            AppBottomBar(
                themeColor
            )
        }
    ) { paddingValues ->

        when(screenViewModel.currentScreen){
            Screen.Home -> HomeScreen(paddingValues, latestReading, aqiValues, overallAQI, themeColor)
            Screen.Analytics -> AnalyticsScreen(navController, paddingValues)
            Screen.Info -> InfoScreen(paddingValues, themeColor)
            Screen.Settings -> SettingsScreen(navController,paddingValues, themeColor)

            else -> HomeScreen(paddingValues, latestReading, aqiValues, overallAQI, themeColor)
        }

    }
}





