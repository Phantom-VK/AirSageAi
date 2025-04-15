package com.vikram.airsageai.utils

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vikram.airsageai.R
import com.vikram.airsageai.viewmodels.GasDataViewModel

class AQINotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val gasDataViewModel : GasDataViewModel = GasDataViewModel()

    override suspend fun doWork(): Result {
        // Example: Get your AQI data (hardcoded for now)
        val latestAqi = gasDataViewModel.latestReading.value?.overallAQI()

        val notification = NotificationCompat.Builder(context, "channel_id")
            .setContentTitle("Air Quality Update")
            .setContentText("Current AQI: $latestAqi")
            .setSmallIcon(R.drawable.airsage_logo)
            .setOngoing(true)
            .build()

        manager.notify(100, notification)

        return Result.success()
    }

}
