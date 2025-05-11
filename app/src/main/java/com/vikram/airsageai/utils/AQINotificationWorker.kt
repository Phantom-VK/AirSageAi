package com.vikram.airsageai.utils

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vikram.airsageai.BuildConfig
import com.vikram.airsageai.R
import com.vikram.airsageai.data.dataclass.AirQualityRequest
import com.vikram.airsageai.data.dataclass.GasReading
import com.vikram.airsageai.data.dataclass.Location
import com.vikram.airsageai.data.repository.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AQINotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val locationUtils = LocationUtils(applicationContext)
    private val api = RetrofitInstance.api
    val apiKey = BuildConfig.AIR_QUALITY_API_KEY
    val fullUrl = "https://airquality.googleapis.com/v1/currentConditions:lookup?key=$apiKey"

    override suspend fun doWork(): Result {
        try {
            // Directly fetch the latest reading from Firebase

            val location = locationUtils.getCurrentLocation()
            Log.i("AQINotificationWorker", "Location: $location")

            val response = api.getCurrentConditions(
                fullUrl = fullUrl,
                request = AirQualityRequest(
                    location = Location(location!!.latitude, location.longitude)
                )
            )

            // Calculate AQI
            val latestAqi = response.body()?.indexes?.firstOrNull()?.aqi
            Log.i("AQINotificationWorker", "Latest AQI: $latestAqi")

            // Get AQI category for more detailed notification
            val aqiCategory = GasReading().getAQICategory(latestAqi ?: 0)

            // Create and show notification
            val notification = NotificationCompat.Builder(context, "channel_id")
                .setContentTitle("Air Quality Update")
                .setContentText("Current AQI: $latestAqi - $aqiCategory")
                .setSmallIcon(R.drawable.airsage_logo)
                .setOngoing(true)
                .build()

            withContext(Dispatchers.Main) {
                manager.notify(100, notification)
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("AQINotificationWorker", "Error fetching AQI data", e)
            return Result.failure()
        }
    }

}