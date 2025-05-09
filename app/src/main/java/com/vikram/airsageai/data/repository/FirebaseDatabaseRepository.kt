package com.vikram.airsageai.data.repository

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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sevenDaysAgoDate = dateFormat.format(calendar.time)

        // Initial load of past 7 days data
        gasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // Iterate through date nodes
                    for (dateSnapshot in snapshot.children) {
                        val dateKey = dateSnapshot.key ?: continue

                        // Check if date is within our 7-day window
                        try {
                            val recordDate = dateFormat.parse(dateKey)?.time ?: 0L
                            if (recordDate < sevenDaysAgoMillis) continue
                        } catch (e: Exception) {
                            // If date parsing fails, try to proceed anyway
                            Log.w("FirebaseDatabaseRepository", "Date format issue: $dateKey", e)
                        }

                        // Iterate through time nodes under this date
                        for (timeSnapshot in dateSnapshot.children) {
                            val timeKey = timeSnapshot.key ?: continue

                            // Create a unique key for this reading
                            val uniqueKey = "$dateKey:$timeKey"

                            // Extract gas readings from this time node
                            val benzene = timeSnapshot.child("Benzene").getValue(Long::class.java)?.toDouble() ?: 0.0
                            val ch4 = timeSnapshot.child("CH4").getValue(Long::class.java)?.toDouble() ?: 0.0
                            val co = timeSnapshot.child("CO").getValue(Long::class.java)?.toDouble() ?: 0.0
                            val h2 = timeSnapshot.child("H2").getValue(Long::class.java)?.toDouble() ?: 0.0
                            val lpg = timeSnapshot.child("LPG").getValue(Long::class.java)?.toDouble() ?: 0.0
                            val nh3 = timeSnapshot.child("NH3").getValue(Long::class.java)?.toDouble() ?: 0.0
                            val smoke = timeSnapshot.child("Smoke").getValue(Long::class.java)?.toDouble() ?: 0.0

                            // Format the timestamp
                            val timeString = "$dateKey $timeKey"

                            // Create and store the gas reading
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

                            readingsCache[uniqueKey] = reading
                        }
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
        // For the nested structure, we'll listen for changes at the date level
        childEventListener = object : ChildEventListener {
            override fun onChildAdded(dateSnapshot: DataSnapshot, previousChildName: String?) {
                if (!isInitialLoadComplete) return // Skip if initial load not done

                try {
                    val dateKey = dateSnapshot.key ?: return

                    // Check if this is a new date node
                    processDateNode(dateKey, dateSnapshot)
                } catch (e: Exception) {
                    Log.e("FirebaseDatabaseRepository", "Error processing new date", e)
                }
            }

            override fun onChildChanged(dateSnapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val dateKey = dateSnapshot.key ?: return

                    // Process the changed date node
                    processDateNode(dateKey, dateSnapshot)
                } catch (e: Exception) {
                    Log.e("FirebaseDatabaseRepository", "Error processing changed date", e)
                }
            }

            override fun onChildRemoved(dateSnapshot: DataSnapshot) {
                val dateKey = dateSnapshot.key ?: return

                // Remove all readings for this date from cache
                val keysToRemove = readingsCache.keys.filter { it.startsWith("$dateKey:") }
                keysToRemove.forEach { readingsCache.remove(it) }

                // Update the flow
                if (keysToRemove.isNotEmpty()) {
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

    private fun processDateNode(dateKey: String, dateSnapshot: DataSnapshot) {
        // Calculate timestamp for 7 days ago for filtering
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgoMillis = calendar.timeInMillis
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Check if date is within our 7-day window
        try {
            val recordDate = dateFormat.parse(dateKey)?.time ?: 0L
            if (recordDate < sevenDaysAgoMillis) return
        } catch (e: Exception) {
            // If date parsing fails, try to proceed anyway
            Log.w("FirebaseDatabaseRepository", "Date format issue: $dateKey", e)
        }

        // Process all time nodes under this date
        for (timeSnapshot in dateSnapshot.children) {
            val timeKey = timeSnapshot.key ?: continue

            // Create a unique key for this reading
            val uniqueKey = "$dateKey:$timeKey"

            // Extract gas readings from this time node
            val benzene = timeSnapshot.child("Benzene").getValue(Long::class.java)?.toDouble() ?: 0.0
            val ch4 = timeSnapshot.child("CH4").getValue(Long::class.java)?.toDouble() ?: 0.0
            val co = timeSnapshot.child("CO").getValue(Long::class.java)?.toDouble() ?: 0.0
            val h2 = timeSnapshot.child("H2").getValue(Long::class.java)?.toDouble() ?: 0.0
            val lpg = timeSnapshot.child("LPG").getValue(Long::class.java)?.toDouble() ?: 0.0
            val nh3 = timeSnapshot.child("NH3").getValue(Long::class.java)?.toDouble() ?: 0.0
            val smoke = timeSnapshot.child("Smoke").getValue(Long::class.java)?.toDouble() ?: 0.0

            // Format the timestamp
            val timeString = "$dateKey $timeKey"

            // Create and store the gas reading
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

            readingsCache[uniqueKey] = reading
        }

        // Update the flow
        updateCachedFlow()
    }

    // Helper function to get timestamp in milliseconds from string
    private fun getTimeInMillis(timeString: String): Long {
        return try {
            if (timeString.matches(Regex("\\d+"))) {
                timeString.toLong() // If stored as millis
            } else {
                // Try to parse in the new format "yyyy-MM-dd HH:mm:ss"
                try {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .parse(timeString)?.time ?: 0L
                } catch (e: Exception) {
                    // Fallback to older date format
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .parse(timeString)?.time ?: 0L
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseDatabaseRepository", "Error parsing time: $timeString", e)
            0L
        }
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
        // Create a listener for changes at the top-level date nodes
        val dateEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // Find the most recent date
                    val dates = snapshot.children.mapNotNull { it.key }

                    if (dates.isEmpty()) {
                        trySend(null)
                        return
                    }

                    // Sort dates in descending order (newest first)
                    val sortedDates = dates.sortedDescending()
                    val latestDate = sortedDates.firstOrNull() ?: return

                    // Get reference to the latest date node
                    val latestDateRef = gasRef.child(latestDate)

                    // Get the latest time entry for this date
                    latestDateRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dateSnapshot: DataSnapshot) {
                            try {
                                // Find the most recent time
                                val times = dateSnapshot.children.mapNotNull { it.key }

                                if (times.isEmpty()) {
                                    trySend(null)
                                    return
                                }

                                // Sort times in descending order (newest first)
                                val sortedTimes = times.sortedDescending()
                                val latestTime = sortedTimes.firstOrNull() ?: return

                                // Get the actual reading data
                                val latestReadingSnapshot = dateSnapshot.child(latestTime)

                                // Extract gas readings
                                val benzene = latestReadingSnapshot.child("Benzene").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val ch4 = latestReadingSnapshot.child("CH4").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val co = latestReadingSnapshot.child("CO").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val h2 = latestReadingSnapshot.child("H2").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val lpg = latestReadingSnapshot.child("LPG").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val nh3 = latestReadingSnapshot.child("NH3").getValue(Long::class.java)?.toDouble() ?: 0.0
                                val smoke = latestReadingSnapshot.child("Smoke").getValue(Long::class.java)?.toDouble() ?: 0.0

                                // Format the timestamp
                                val timeString = "$latestDate $latestTime"

                                // Create gas reading
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

                                trySend(reading)
                            } catch (e: Exception) {
                                Log.e("FirebaseDatabaseRepository", "Error fetching latest time", e)
                                trySend(null)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("FirebaseDatabaseRepository", "Latest time fetch cancelled", error.toException())
                            trySend(null)
                        }
                    })
                } catch (e: Exception) {
                    Log.e("FirebaseDatabaseRepository", "Error fetching latest date", e)
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDatabaseRepository", "Latest date fetch cancelled", error.toException())
                close(error.toException())
            }
        }

        gasRef.addValueEventListener(dateEventListener)

        awaitClose {
            gasRef.removeEventListener(dateEventListener)
        }
    }

    override fun getLast7DaysReadings(): Flow<List<GasReading>> {
        // Return the cached readings as a flow
        return cachedReadingsFlow.asStateFlow()
    }

}