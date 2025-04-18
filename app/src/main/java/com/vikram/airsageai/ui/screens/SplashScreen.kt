package com.vikram.airsageai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.vikram.airsageai.R
import kotlinx.coroutines.delay


@Composable
fun SplashScreen() {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0XFFEDECE7)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.airsage_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp)
        )
    }
}
