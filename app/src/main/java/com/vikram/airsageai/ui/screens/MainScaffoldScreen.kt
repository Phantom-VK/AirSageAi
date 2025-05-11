package com.vikram.airsageai.ui.screens

import LocationViewModel
import LocationViewModelFactory
import android.Manifest
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.vikram.airsageai.ui.components.AppBottomBar
import com.vikram.airsageai.utils.LocationUtils
import com.vikram.airsageai.viewmodels.GasDataViewModel
import com.vikram.airsageai.viewmodels.ScreenViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScaffoldScreen() {
    val navController = rememberNavController()

    val screenViewModel: ScreenViewModel = viewModel()
    val gasDataViewModel: GasDataViewModel = hiltViewModel()
    val context = LocalContext.current
    val locationUtils = remember { LocationUtils(context) }
    val viewModelFactory = remember { LocationViewModelFactory(locationUtils, context) }
    val locationVM: LocationViewModel = viewModel(
        key = "LocationVM",
        factory = viewModelFactory
    )


    val location = locationVM.location.collectAsState()
    Log.d("AirsageTest", "location inside mainscaffold: ${location.value?.latitude} ${location.value?.longitude}")


    // Collect data states
    val latestReadingState = gasDataViewModel.latestReadingState.collectAsState()
    val weeklyReadingState = gasDataViewModel.weeklyReadingState.collectAsState()

    // Create a wrapped State for the extracted data to match the expected types
    val latestReading = (latestReadingState.value as? GasDataViewModel.DataState.Success)?.data


    // Determine AQI based on latest reading state
//    val overallAQI = when (val state = latestReadingState.value) {
//        is GasDataViewModel.DataState.Success -> state.data?.overallAQI()
//        else -> null
//    }
    var overallAQI by remember { mutableIntStateOf(0) }



    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    if (permissionsState.allPermissionsGranted.not()) {
        SideEffect {
            permissionsState.launchMultiplePermissionRequest()
        }
    }


    LaunchedEffect(location.value?.latitude to location.value?.longitude) {
        location.value?.let {
            gasDataViewModel.getAqi(it.latitude, it.longitude)?.let { aqi ->
                overallAQI = aqi
                Log.d("AirsageTest", "Fetched AQI: $aqi")
            }
        }
    }




    val themeColor by remember(overallAQI) {
        mutableStateOf(
            when (overallAQI) {
                in 0..50 -> Color(0xFF96D9F3)
                in 51..100 -> Color(0xFFFCEFCC)
                else -> Color(0xFFFFC2A5)
            }
        )
    }


    Scaffold(
        bottomBar = {
            AppBottomBar(themeColor)
        }
    ) { paddingValues ->
        // Check if any data is still loading
        val isLoading = latestReadingState.value is GasDataViewModel.DataState.Loading ||
                weeklyReadingState.value is GasDataViewModel.DataState.Loading

        // Check for errors
        val hasError = latestReadingState.value is GasDataViewModel.DataState.Error ||
                weeklyReadingState.value is GasDataViewModel.DataState.Error

        when {
            isLoading -> {
                LoadingScreen(themeColor)
            }
            hasError -> {
                ErrorScreen(
                    latestReadingError = (latestReadingState.value as? GasDataViewModel.DataState.Error)?.message,
                    weeklyReadingError = (weeklyReadingState.value as? GasDataViewModel.DataState.Error)?.message,
                    onRetryLatest = { gasDataViewModel.retryLatestReading() },
                    onRetryWeekly = { gasDataViewModel.retryWeeklyReadings() },
                    themeColor = themeColor
                )
            }
            else -> {
                val last7DaysReading = remember {
                    generateDummyGasReadings(48, 30) // 48 readings, 30 minutes each
                }
//                    (weeklyReadingState.value as? GasDataViewModel.DataState.Success)?.data ?: emptyList()

                when (screenViewModel.currentScreen) {
                    Screen.Home -> HomeScreen(paddingValues, latestReading, overallAQI, themeColor)
                    Screen.Analytics -> AnalyticsScreen(paddingValues, themeColor, last7DaysReading)
                    Screen.Info -> InfoScreen(paddingValues, themeColor)
                    Screen.Settings -> SettingsScreen(navController, paddingValues, themeColor)
                    else -> HomeScreen(paddingValues, latestReading, overallAQI, themeColor)
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(themeColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColor.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = themeColor,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading air quality data...",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please wait while we fetch the latest readings",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
fun ErrorScreen(
    latestReadingError: String?,
    weeklyReadingError: String?,
    onRetryLatest: () -> Unit,
    onRetryWeekly: () -> Unit,
    themeColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unable to load data",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show specific error messages if available
            latestReadingError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onRetryLatest,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text("Retry Loading Latest Reading")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            weeklyReadingError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onRetryWeekly,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text("Retry Loading Weekly Readings")
                }
            }

            // If no specific errors, show a general retry button
            if (latestReadingError == null && weeklyReadingError == null) {
                Button(
                    onClick = {
                        onRetryLatest()
                        onRetryWeekly()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text("Retry")
                }
            }
        }
    }
}





