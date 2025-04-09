package com.vikram.airsageai.ui.screens

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SplashScreen : Screen("splash_screen")
    object Analytics : Screen("analytics")
    object Info : Screen("info")
    object Settings : Screen("settings")
    object MainScaffold : Screen("main_scaffold")
}