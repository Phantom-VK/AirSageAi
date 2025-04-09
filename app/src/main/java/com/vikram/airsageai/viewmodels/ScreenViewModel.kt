package com.vikram.airsageai.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.vikram.airsageai.ui.screens.Screen

class ScreenViewModel: ViewModel() {

    private val _currentScreen = mutableStateOf<Screen>(Screen.Home)
    val currentScreen: Screen
        get() = _currentScreen.value

    fun setCurrentScreen(screen: Screen) {
        _currentScreen.value = screen
    }


}