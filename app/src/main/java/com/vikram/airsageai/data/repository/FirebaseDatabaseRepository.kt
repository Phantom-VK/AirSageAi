package com.vikram.airsageai.data.repository

import ConversionUtils
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vikram.airsageai.data.dataclass.GasReading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDatabaseRepository @Inject constructor() : DatabaseRepository {
    private val database = Firebase.database
    private val gasRef = database.getReference("gas_logs")
    private val converter = ConversionUtils()

    // Cache for storing readings
    private val readingsCache = ConcurrentHashMap<String, GasReading>()
    private val cachedReadingsFlow = MutableStateFlow<List<GasReading>>(emptyList())
    private var isInitialLoadComplete = false
    private var childEventListener: ChildEventListener? = null

    init {
        // Initialize cache with data and set up listeners
        initializeCache()
    }

    private fun initializeCache() {
        // First clear any existing data
        readingsCache.clear()
        isInitialLoadComplete = false

        // Remove existing listener if any
        childEventListener?.let {
            gasRef.removeEventListener(it)
            childEventListener = null
        }

        // Calculate timestamp for 7 days ago
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgoMillis = calendar.timeInMillis

        // Initial load of past 7 days data
        gasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    for (data in snapshot.children) {
                        val readingMap = data.value as? Map<String, Any> ?: continue
                        val timeString = readingMap["Time"]?.toString() ?: continue

                        // Skip if older than 7 days
                        val recordTime = getTimeInMillis(timeString)
                        if (recordTime < sevenDaysAgoMillis) continue

                        // Add to cache
                        val reading = convertToGasReading(readingMap)
                        readingsCache[data.key ?: continue] = reading
                    }

                    // Update the flow with initial data
                    updateCachedFlow()
                    isInitialLoadComplete = true

                    // Set up listener for future changes
                    setupChildEventListener()

                } catch (e: Exception) {
                    Log.e("FirebaseDatabaseRepository", "Error initializing cache", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseRepository", "Initial load cancelled", error.toException())
                isInitialLoadComplete = true // Mark as complete so we can try again later
            }
        })
    }

    private fun setupChildEventListener() {
        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (!isInitialLoadComplete) return // Skip if initial load not done

                try {
                    val readingMap = snapshot.value as? Map<String, Any> ?: return
                    val timeString = readingMap["Time"]?.toString() ?: return

                    // Skip if older than 7 days
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    val sevenDaysAgoMillis = calendar.timeInMillis

                    val recordTime = getTimeInMillis(timeString)
                    if (recordTime < sevenDaysAgoMillis) return

                    // Add to cache if new
                    val key = snapshot.key ?: return
                    if (!readingsCache.containsKey(key)) {
                        readingsCache[key] = convertToGasReading(readingMap)
                        updateCachedFlow()
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDatabaseRepository", "Error processing new reading", e)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val key = snapshot.key ?: return
                    val readingMap = snapshot.value as? Map<String, Any> ?: return

                    // Update in cache if exists
                    if (readingsCache.containsKey(key)) {
                        readingsCache[key] = convertToGasReading(readingMap)
                        updateCachedFlow()
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDatabaseRepository", "Error processing changed reading", e)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val key = snapshot.key ?: return
                if (readingsCache.containsKey(key)) {
                    readingsCache.remove(key)
                    updateCachedFlow()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Not needed for this implementation
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseRepository", "Child event cancelled", error.toException())
            }
        }

        // Register the listener
        childEventListener?.let {
            gasRef.addChildEventListener(it)
        }
    }

    // Helper function to get timestamp in milliseconds from string
    private fun getTimeInMillis(timeString: String): Long {
        return try {
            if (timeString.matches(Regex("\\d+"))) {
                timeString.toLong() // If stored as millis
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .parse(timeString)?.time ?: 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    // Helper function to convert Firebase data to GasReading object
    private fun convertToGasReading(readingMap: Map<String, Any>): GasReading {
        val rawCO = readingMap["CO"]?.toString()?.toInt() ?: 0
        val rawBenzene = readingMap["Benzene"]?.toString()?.toInt() ?: readingMap["Benzen"]?.toString()?.toInt() ?: 0
        val rawNH3 = readingMap["NH3"]?.toString()?.toInt() ?: 0
        val rawSmoke = readingMap["Smoke"]?.toString()?.toInt() ?: 0
        val rawLPG = readingMap["LPG"]?.toString()?.toInt() ?: 0
        val rawCH4 = readingMap["CH4"]?.toString()?.toInt() ?: 0
        val rawH2 = readingMap["H2"]?.toString()?.toInt() ?: 0

        return GasReading(
            CO = converter.convertCO(rawCO),
            Benzene = converter.convertBenzene(rawBenzene),
            NH3 = converter.convertNH3(rawNH3),
            Smoke = converter.convertSmoke(rawSmoke),
            LPG = converter.convertLPG(rawLPG),
            CH4 = converter.convertCH4(rawCH4),
            H2 = converter.convertH2(rawH2),
            Time = readingMap["Time"].toString()
        )
    }

    // Update the flow with current cached data (sorted)
    private fun updateCachedFlow() {
        // Clean up old readings (older than 7 days)
        cleanupOldReadings()

        // Sort by time (newest first)
        val sortedReadings = readingsCache.values.sortedByDescending {
            getTimeInMillis(it.Time ?: "0")
        }

        // Update the flow
        cachedReadingsFlow.value = sortedReadings
    }

    // Remove readings older than 7 days
    private fun cleanupOldReadings() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgoMillis = calendar.timeInMillis

        val keysToRemove = mutableListOf<String>()

        for ((key, reading) in readingsCache) {
            val recordTime = getTimeInMillis(reading.Time ?: "0")
            if (recordTime < sevenDaysAgoMillis) {
                keysToRemove.add(key)
            }
        }

        for (key in keysToRemove) {
            readingsCache.remove(key)
        }
    }

    override fun getLatestGasReading(): Flow<GasReading?> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val readingMap = data.value as Map<String, Any>

                    try {
                        // Get raw analog values
                        val rawCO = readingMap["CO"]?.toString()?.toInt() ?: 0
                        val rawBenzene = readingMap["Benzene"]?.toString()?.toInt() ?: 0
                        val rawNH3 = readingMap["NH3"]?.toString()?.toInt() ?: 0
                        val rawSmoke = readingMap["Smoke"]?.toString()?.toInt() ?: 0
                        val rawLPG = readingMap["LPG"]?.toString()?.toInt() ?: 0
                        val rawCH4 = readingMap["CH4"]?.toString()?.toInt() ?: 0
                        val rawH2 = readingMap["H2"]?.toString()?.toInt() ?: 0

                        // Convert raw values to ppm
                        val reading = GasReading(
                            CO = converter.convertCO(rawCO),
                            Benzene = converter.convertBenzene(rawBenzene),
                            NH3 = converter.convertNH3(rawNH3),
                            Smoke = converter.convertSmoke(rawSmoke),
                            LPG = converter.convertLPG(rawLPG),
                            CH4 = converter.convertCH4(rawCH4),
                            H2 = converter.convertH2(rawH2),
                            Time = readingMap["Time"].toString()
                        )
                        trySend(reading)
                    } catch (e: Exception) {
                        Log.e("FirebaseDatabaseRepository", "Error converting gas readings", e)
                        trySend(null)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        gasRef.limitToLast(1).addValueEventListener(valueEventListener)

        awaitClose {
            gasRef.removeEventListener(valueEventListener)
        }
    }

    override fun getLast7DaysReadings(): Flow<List<GasReading>> {
        // Return the cached readings as a flow
        return cachedReadingsFlow.asStateFlow()
    }

    override suspend fun saveGasReading(reading: GasReading) {
        gasRef.push().setValue(reading).await()
    }
}