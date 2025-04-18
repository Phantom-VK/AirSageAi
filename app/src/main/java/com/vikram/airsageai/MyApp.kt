package com.vikram.airsageai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.vikram.airsageai.utils.AQINotificationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit


@HiltAndroidApp
class MyApp : Application()  {



    override fun onCreate() {
        super.onCreate()


        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id",
                "AirSage AQI",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(channel)
        }


    }

     fun scheduleAQINotificationWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<AQINotificationWorker>(
            15, TimeUnit.MINUTES // Minimum interval for WorkManager
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "AQI_NOTIFICATION_WORK",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelAQINotificationWorker(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("AQI_NOTIFICATION_WORK")
    }


}

