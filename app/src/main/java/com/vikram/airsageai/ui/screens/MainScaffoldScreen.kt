package com.vikram.airsageai.ui.screens

import android.Manifest
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.vikram.airsageai.ui.components.AppBottomBar
import com.vikram.airsageai.viewmodels.GasDataViewModel
import com.vikram.airsageai.viewmodels.ScreenViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScaffoldScreen(){
    val navController = rememberNavController()

    val screenViewModel: ScreenViewModel = viewModel()
    val gasDataViewModel: GasDataViewModel = hiltViewModel()
    val latestReading = gasDataViewModel.latestReading.collectAsState()
    val overallAQI = gasDataViewModel.latestReading.collectAsState().value?.overallAQI()


    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    LaunchedEffect(permissionsState.allPermissionsGranted) {

        if (!permissionsState.permissions.any { it.status.isGranted }) {
            permissionsState.launchMultiplePermissionRequest()
        }

    }



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
            Screen.Home -> HomeScreen(paddingValues, latestReading = latestReading, overallAQI, themeColor)
            Screen.Analytics -> AnalyticsScreen(navController, paddingValues)
            Screen.Info -> InfoScreen(paddingValues, themeColor)
            Screen.Settings -> SettingsScreen(navController,paddingValues, themeColor)

            else -> HomeScreen(paddingValues, latestReading, overallAQI, themeColor)
        }

    }
}





