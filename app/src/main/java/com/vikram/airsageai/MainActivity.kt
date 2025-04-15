package com.vikram.airsageai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.vikram.airsageai.ui.navigation.Navigation
import com.vikram.airsageai.ui.screens.SplashScreen
import com.vikram.airsageai.viewmodels.GasDataViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val gasDataViewModel: GasDataViewModel = viewModel()
            val navController = rememberNavController()
            val context = LocalContext.current

            // State for tracking permission status
            var permissionState by remember {
                mutableStateOf(PermissionState.CHECKING)
            }

            // State for showing rationale dialog
            var showRationale by remember { mutableStateOf(false) }

            // For Android 10+, background location must be requested separately
            val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                permissionState = if (isGranted) {
                    PermissionState.GRANTED
                } else {
                    showRationale = true
                    PermissionState.DENIED
                }
            }

            // Request location permissions
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val locationPermissionsGranted = permissions.entries.all { it.value }

                if (locationPermissionsGranted) {
                    // If basic location permissions granted, check if we need background location
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val hasBackgroundLocation = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasBackgroundLocation) {
                            permissionState = PermissionState.GRANTED
                        } else {
                            // Launch background location request separately
                            backgroundLocationPermissionLauncher.launch(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                        }
                    } else {
                        permissionState = PermissionState.GRANTED
                    }
                } else {
                    // Show rationale if permission was denied
                    showRationale = true
                    permissionState = PermissionState.DENIED
                }
            }

            // Notification permission for Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { _ ->
                // We continue regardless of notification permission
                // Just requesting location permissions after this
                val locationPermissions = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                locationPermissionLauncher.launch(locationPermissions)
            }

            // Check and request permissions when app starts
            LaunchedEffect(Unit) {
                // Check current permission status
                val hasFineLocation = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val hasCoarseLocation = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val hasBackgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                } else true

                if (hasFineLocation && hasCoarseLocation && hasBackgroundLocation) {
                    permissionState = PermissionState.GRANTED
                } else {
                    // Start permission request flow
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // For Android 13+, request notification permission first
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // For older Android versions, directly request location
                        val locationPermissions = arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        locationPermissionLauncher.launch(locationPermissions)
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (permissionState) {
                    PermissionState.CHECKING -> {
                        // Show loading or initial state
                        SplashScreen(
                            navController
                        )
                    }
                    PermissionState.GRANTED -> {
                        // Main app content when permissions are granted
                        Navigation(navController, gasDataViewModel)
                    }
                    PermissionState.DENIED -> {
                        // Show permission request screen
                        PermissionRequestScreen(
                            onRequestPermission = {
                                val locationPermissions = arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                                locationPermissionLauncher.launch(locationPermissions)
                            }
                        )
                    }
                }

                // Show rationale dialog if needed
                if (showRationale) {
                    AlertDialog(
                        onDismissRequest = { showRationale = false },
                        title = { Text("Location Permission Required") },
                        text = {
                            Text("This app needs location permissions to function properly. " +
                                    "Please grant location permissions in app settings.")
                        },
                        confirmButton = {
                            Button(onClick = {
                                showRationale = false
                                // Open app settings
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }) {
                                Text("Open Settings")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showRationale = false }) {
                                Text("Later")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Location Permission Required",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This app needs location permissions to show air quality data for your area. " +
                    "Please grant the permission to continue."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRequestPermission) {
            Text("Grant Permissions")
        }
    }
}

enum class PermissionState {
    CHECKING,
    GRANTED,
    DENIED
}