package com.vikram.airsageai.utils

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vikram.airsageai.R
import com.vikram.airsageai.data.dataclass.GasReading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AQINotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val database = Firebase.database
    private val gasRef = database.getReference("test_data")

    override suspend fun doWork(): Result {
        try {
            // Directly fetch the latest reading from Firebase
            val latestReading = fetchLatestGasReadingFromFirebase()

            // Calculate AQI
            val latestAqi = latestReading?.overallAQI()

            // Create and show notification
            val notification = NotificationCompat.Builder(context, "channel_id")
                .setContentTitle("Air Quality Update")
                .setContentText("Current AQI: $latestAqi")
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

    private suspend fun fetchLatestGasReadingFromFirebase(): GasReading? = suspendCancellableCoroutine { continuation ->
        val listener = gasRef.limitToLast(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    var reading: GasReading? = null

                    for (data in snapshot.children) {
                        val readingMap = data.value as? Map<String, Any>
                        Log.d("AQIWorker", "Received data: $readingMap")

                        if (readingMap != null) {
                            reading = GasReading(
                                CO_PPM = readingMap["CO (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                CO2_PPM = readingMap["CO2 (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                NH3_PPM = readingMap["NH3 (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                NOx_PPM = readingMap["NOx (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                LPG_PPM = readingMap["LPG (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                Methane_PPM = readingMap["Methane (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                Hydrogen_PPM = readingMap["Hydrogen (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0
                            )
                            Log.d("AQIWorker", "Parsed reading: $reading")
                        }
                    }

                    // Resume with reading result (might be null if no data)
                    if (!continuation.isCompleted) {
                        continuation.resume(reading)
                    }
                } catch (e: Exception) {
                    Log.e("AQIWorker", "Parsing failed", e)
                    if (!continuation.isCompleted) {
                        continuation.resumeWithException(e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AQIWorker", "Data fetch cancelled", error.toException())
                if (!continuation.isCompleted) {
                    continuation.resumeWithException(error.toException())
                }
            }
        })

        // Make sure we remove the listener when the coroutine is cancelled
        continuation.invokeOnCancellation {
            gasRef.removeEventListener(listener)
        }
    }
}