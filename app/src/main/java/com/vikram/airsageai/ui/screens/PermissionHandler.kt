package com.vikram.airsageai.ui.screens

import LocationViewModel
import LocationViewModelFactory
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vikram.airsageai.data.dataclass.PermissionState
import com.vikram.airsageai.ui.navigation.Navigation
import com.vikram.airsageai.utils.LocationUtils
import com.vikram.airsageai.viewmodels.GasDataViewModel

@Composable
fun PermissionHandler(
    navController: NavHostController
) {
    val context = LocalContext.current
    val locationUtils = remember { LocationUtils(context) }
    val viewModelFactory = remember { LocationViewModelFactory(locationUtils, context) }
    val locationVM: LocationViewModel = viewModel(factory = viewModelFactory)

    // State for tracking permission status
    var permissionState by remember { mutableStateOf(PermissionState.CHECKING) }

    // State for showing rationale dialog
    var showRationale by remember { mutableStateOf(false) }

    // Define permission launchers
    val permissionLaunchers = setupPermissionLaunchers(
        context = context,
        onPermissionStateChange = { permissionState = it },
        onShowRationale = { showRationale = it }
    )

    // Check and request permissions when composable launches
    LaunchedEffect(Unit) {
        checkAndRequestPermissions(
            context = context,
            permissionLaunchers = permissionLaunchers,
            onPermissionStateChange = { permissionState = it }
        )
    }

    // Show appropriate screen based on permission state
    when (permissionState) {
        PermissionState.CHECKING -> {
            SplashScreen()
        }
        PermissionState.GRANTED -> {
            Navigation(navController)
        }
        PermissionState.DENIED -> {
            PermissionRequestScreen {
                val locationPermissions = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                permissionLaunchers.locationPermissionLauncher.launch(locationPermissions)
            }
        }
    }

    // Show rationale dialog if needed
    if (showRationale) {
        PermissionRationaleDialog(
            onDismiss = { showRationale = false },
            onOpenSettings = {
                showRationale = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Permission Required") },
        text = {
            Text(
                "This app needs location permissions to function properly. " +
                        "Please grant location permissions in app settings."
            )
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}

data class PermissionLaunchers(
    val backgroundLocationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    val locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    val notificationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
)

@Composable
private fun setupPermissionLaunchers(
    context: android.content.Context,
    onPermissionStateChange: (PermissionState) -> Unit,
    onShowRationale: (Boolean) -> Unit
): PermissionLaunchers {
    // For Android 10+, background location must be requested separately
    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionStateChange(PermissionState.GRANTED)
        } else {
            onShowRationale(true)
            onPermissionStateChange(PermissionState.DENIED)
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
                    onPermissionStateChange(PermissionState.GRANTED)
                } else {
                    // Launch background location request separately
                    backgroundLocationPermissionLauncher.launch(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                }
            } else {
                onPermissionStateChange(PermissionState.GRANTED)
            }
        } else {
            // Show rationale if permission was denied
            onShowRationale(true)
            onPermissionStateChange(PermissionState.DENIED)
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

    return PermissionLaunchers(
        backgroundLocationPermissionLauncher,
        locationPermissionLauncher,
        notificationPermissionLauncher
    )
}

private fun checkAndRequestPermissions(
    context: android.content.Context,
    permissionLaunchers: PermissionLaunchers,
    onPermissionStateChange: (PermissionState) -> Unit
) {
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
        onPermissionStateChange(PermissionState.GRANTED)
    } else {
        // Start permission request flow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+, request notification permission first
            permissionLaunchers.notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            // For older Android versions, directly request location
            val locationPermissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            permissionLaunchers.locationPermissionLauncher.launch(locationPermissions)
        }
    }
}