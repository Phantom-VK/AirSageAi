package com.vikram.airsageai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vikram.airsageai.ui.components.GasPpmGauge
import com.vikram.airsageai.ui.navigation.Navigation
import com.vikram.airsageai.ui.screens.HomeScreen
import com.vikram.airsageai.ui.screens.SplashContent
import com.vikram.airsageai.ui.theme.AirSageAiTheme
import com.vikram.airsageai.utils.Database
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

         val database = Database()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            AppContent(navController)

        }
    }
}

@Composable
fun AppContent(navController: NavController) {
    // State to control when the app is ready
    var isAppReady by remember { mutableStateOf(false) }

    // Simulate loading (replace with your actual loading logic)
    LaunchedEffect(Unit) {
        delay(2000) // Simulate 2 seconds of loading
        isAppReady = true
    }

    if (isAppReady) {
        HomeScreen(navController)
    } else {
        SplashContent()
    }
}