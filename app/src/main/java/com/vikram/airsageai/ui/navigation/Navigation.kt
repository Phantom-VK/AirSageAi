package com.vikram.airsageai.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost

@Composable
fun Navigation(
    navController: NavHostController,

) {
    NavHost(
        navController = navController,
        startDestination = "splash_screen"
    ) {


    }
}