package com.vikram.airsageai.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.vikram.airsageai.data.dataclass.PermissionState
import com.vikram.airsageai.ui.navigation.Navigation

@Composable
fun PermissionHandler(navController: NavHostController) {
    val context = LocalContext.current

    var permissionState by remember { mutableStateOf(PermissionState.CHECKING) }
    var showRationale by remember { mutableStateOf(false) }

    val permissionLaunchers = setupPermissionLaunchers(
        context = context,
        onPermissionStateChange = { permissionState = it },
        onShowRationale = { showRationale = it }
    )

    LaunchedEffect(Unit) {
        checkAndRequestPermissions(
            context = context,
            permissionLaunchers = permissionLaunchers,
            onPermissionStateChange = { permissionState = it }
        )
    }

    when (permissionState) {
        PermissionState.CHECKING -> SplashScreen()
        PermissionState.GRANTED -> Navigation(navController)
        PermissionState.DENIED -> {
            PermissionRequestScreen {
                val permissions = getRequiredPermissions()
                permissionLaunchers.locationPermissionLauncher.launch(permissions)
            }
        }
    }

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
        title = { Text("Permissions Required") },
        text = {
            Text(
                "This app needs location and storage permissions to function properly. " +
                        "Location is needed for air quality data, and storage is needed to save reports."
            )
        },
        confirmButton = {
            Button(onClick = onOpenSettings) { Text("Open Settings") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Later") }
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
    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onPermissionStateChange(PermissionState.GRANTED)
        else {
            onShowRationale(true)
            onPermissionStateChange(PermissionState.DENIED)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }

        if (allGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val hasBackground = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasBackground) {
                    backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    onPermissionStateChange(PermissionState.GRANTED)
                }
            } else {
                onPermissionStateChange(PermissionState.GRANTED)
            }
        } else {
            onShowRationale(true)
            onPermissionStateChange(PermissionState.DENIED)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        val permissions = getRequiredPermissions()
        locationPermissionLauncher.launch(permissions)
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
    val permissions = getRequiredPermissions()
    val allGranted = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    val hasBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    } else true

    if (allGranted && hasBackground) {
        onPermissionStateChange(PermissionState.GRANTED)
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLaunchers.notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissionLaunchers.locationPermissionLauncher.launch(permissions)
        }
    }
}

private fun getRequiredPermissions(): Array<String> {
    val basePermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        basePermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        basePermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    } else {
        // Android 11+ handles storage differently (scoped storage)
        // You may want to use SAF or MediaStore APIs instead of asking permission
        // Optional: Request `MANAGE_EXTERNAL_STORAGE` via special intent (Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
    }

    return basePermissions.toTypedArray()
}