package com.vikram.airsageai.utils

import LocationViewModel
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
import com.vikram.airsageai.data.dataclass.AirQualityRequest
import com.vikram.airsageai.data.dataclass.GasReading
import com.vikram.airsageai.data.dataclass.Location
import com.vikram.airsageai.data.repository.RetrofitInstance
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
    private val gasRef = database.getReference("gas_logs")
    val locationUtils = LocationUtils(context)
    val locationVM: LocationViewModel = LocationViewModel(locationUtils, context)

    private val api = RetrofitInstance.api
    private val fullUrl = "https://airquality.googleapis.com/v1/currentConditions:lookup?key=AIzaSyCeUGG8Ks7tks33kyzBZu23rKPH354l07Q"
    val location = locationVM.location.value
    override suspend fun doWork(): Result {
        try {
            // Directly fetch the latest reading from Firebase
            val latestReading = fetchLatestGasReadingFromFirebase()

            val response = api.getCurrentConditions(
                fullUrl = fullUrl,
                request = AirQualityRequest(
                    location = Location(location!!.latitude, location.longitude)
                )
            )

            // Calculate AQI
//            val latestAqi = latestReading?.overallAQI()
            val latestAqi = response.body()?.indexes?.firstOrNull()?.aqi

            // Get AQI category for more detailed notification
            val aqiCategory = latestReading?.getAQICategory(latestAqi ?: 0) ?: "Unknown"

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

    private suspend fun fetchLatestGasReadingFromFirebase(): GasReading? = suspendCancellableCoroutine { continuation ->
        // First, we need to get the latest date
        val dateListener = gasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // Convert date nodes to a sorted list (newest first)
                    val dateNodes = snapshot.children.mapNotNull { it.key }.sorted().reversed()

                    if (dateNodes.isEmpty()) {
                        if (!continuation.isCompleted) {
                            continuation.resume(null)
                        }
                        return
                    }

                    // Get the most recent date
                    val latestDate = dateNodes.first()

                    // Now get the latest time for this date
                    gasRef.child(latestDate).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dateSnapshot: DataSnapshot) {
                            try {
                                // Convert time nodes to a sorted list (newest first)
                                val timeNodes = dateSnapshot.children.mapNotNull { it.key }.sorted().reversed()

                                if (timeNodes.isEmpty()) {
                                    if (!continuation.isCompleted) {
                                        continuation.resume(null)
                                    }
                                    return
                                }

                                // Get the most recent time
                                val latestTime = timeNodes.first()

                                // Get the reading data for this time
                                val readingSnapshot = dateSnapshot.child(latestTime)

                                // Extract gas readings
                                val benzene = readingSnapshot.child("Benzene").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val ch4 = readingSnapshot.child("CH4").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val co = readingSnapshot.child("CO").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val h2 = readingSnapshot.child("H2").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val lpg = readingSnapshot.child("LPG").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val nh3 = readingSnapshot.child("NH3").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val smoke = readingSnapshot.child("Smoke").getValue(Long::class.java)?.toDouble() ?: 0.0

                                // Format the timestamp
                                val timeString = "$latestDate $latestTime"

                                // Create the gas reading object
                                val reading = GasReading(
                                    CO = co,
                                    Benzene = benzene,
                                    NH3 = nh3,
                                    Smoke = smoke,
                                    LPG = lpg,
                                    CH4 = ch4,
                                    H2 = h2,
                                    Time = timeString
                                )

                                Log.d("AQIWorker", "Parsed reading: $reading")

                                if (!continuation.isCompleted) {
                                    continuation.resume(reading)
                                }
                            } catch (e: Exception) {
                                Log.e("AQIWorker", "Error processing time node", e)
                                if (!continuation.isCompleted) {
                                    continuation.resumeWithException(e)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("AQIWorker", "Time fetch cancelled", error.toException())
                            if (!continuation.isCompleted) {
                                continuation.resumeWithException(error.toException())
                            }
                        }
                    })
                } catch (e: Exception) {
                    Log.e("AQIWorker", "Error processing date nodes", e)
                    if (!continuation.isCompleted) {
                        continuation.resumeWithException(e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AQIWorker", "Date fetch cancelled", error.toException())
                if (!continuation.isCompleted) {
                    continuation.resumeWithException(error.toException())
                }
            }
        })

        // Make sure we remove the listener when the coroutine is cancelled
        continuation.invokeOnCancellation {
            gasRef.removeEventListener(dateListener)
        }
    }
}