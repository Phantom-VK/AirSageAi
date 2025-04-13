package com.vikram.airsageai.utils

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vikram.airsageai.R

class AQINotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    override suspend fun doWork(): Result {
        // Example: Get your AQI data (hardcoded for now)
        val currentAQI = 75 // Replace with real fetch if needed

        val notification = NotificationCompat.Builder(context, "channel_id")
            .setContentTitle("Air Quality Update")
            .setContentText("Current AQI: $currentAQI")
            .setSmallIcon(R.drawable.airsage_logo)
            .setOngoing(true)
            .build()

        manager.notify(100, notification)

        return Result.success()
    }

}
