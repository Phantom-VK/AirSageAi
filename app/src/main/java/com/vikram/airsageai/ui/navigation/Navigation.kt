package com.vikram.airsageai.ui.navigation


import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vikram.airsageai.ui.screens.MainScaffoldScreen
import com.vikram.airsageai.ui.screens.Screen
import com.vikram.airsageai.ui.screens.SplashScreen
import com.vikram.airsageai.viewmodels.GasDataViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    gasDataViewModel: GasDataViewModel
) {

    val latestReading = gasDataViewModel.latestReading.value
    val aqiValues = latestReading?.toAQI()
    val overallAQI = latestReading?.overallAQI()


    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {

        composable(Screen.MainScaffold.route){
            MainScaffoldScreen(
                latestReading = latestReading,
                aqiValues = aqiValues,
                overallAQI = overallAQI
            )
        }

        composable(Screen.SplashScreen.route){
            SplashScreen(navController)

        }




    }
}