package com.vikram.airsageai.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vikram.airsageai.R


class AirSageNotificationService (
    private val context: Context
){
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager


    fun showNotification(contentText:String, contentTitle:String){
        val notification = NotificationCompat.Builder(context, "channel_id")
            .setContentText(contentText)
            .setContentTitle(contentTitle)
            .setSmallIcon(R.drawable.airsage_logo)
            .setOngoing(true)
            .build()
        Log.d("MainScaffoldScreen", "Notifications Showed")
        notificationManager.notify(1, notification)
    }
    fun hideNotification(){
        notificationManager.cancel(1)

    }
}