package com.vikram.airsageai.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vikram.airsageai.ui.screens.MainScaffoldScreen
import com.vikram.airsageai.ui.screens.Screen
import com.vikram.airsageai.ui.screens.SplashScreen

@Composable
fun Navigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {

        composable(Screen.MainScaffold.route){
            MainScaffoldScreen()
        }

        composable(Screen.SplashScreen.route){
            SplashScreen(navController)

        }




    }
}