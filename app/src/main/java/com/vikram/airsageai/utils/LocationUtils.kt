package com.vikram.airsageai.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

class LocationUtils(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        // First try getting the last known location as it's faster
        val lastLocation = try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            Log.e("LocationUtils", "Error getting last location", e)
            null
        }

        // If last location is available and recent (less than 2 minutes old), use it
        if (lastLocation != null && System.currentTimeMillis() - lastLocation.time < 2 * 60 * 1000) {
            return lastLocation
        }

        // Otherwise request a fresh location update
        return requestFreshLocation()
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdateDelayMillis(15000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                val location = result.lastLocation
                continuation.resume(location)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )

            // Ensure we clean up if coroutine is cancelled
            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } catch (e: Exception) {
            Log.e("LocationUtils", "Error requesting location updates", e)
            continuation.resume(null)
        }
    }

    fun getLocationName(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                // Build a more detailed location name if possible
                val locality = address.locality
                val subAdminArea = address.subAdminArea
                val adminArea = address.adminArea

                return when {
                    locality != null && adminArea != null -> "$locality, $adminArea"
                    locality != null -> locality
                    subAdminArea != null -> subAdminArea
                    adminArea != null -> adminArea
                    else -> "Unknown Location"
                }
            }
        } catch (e: IOException) {
            Log.e("LocationUtils", "Error getting location name", e)
        } catch (e: Exception) {
            Log.e("LocationUtils", "Unexpected error", e)
        }
        return "Location name unavailable"
    }
}