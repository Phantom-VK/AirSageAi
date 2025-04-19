package com.vikram.airsageai.data.repository

import ConversionUtils
import android.icu.lang.UCharacter.LineBreak.H2
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vikram.airsageai.data.dataclass.GasReading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseDatabaseRepository @Inject constructor() : DatabaseRepository {
    private val database = Firebase.database
    private val gasRef = database.getReference("gas_logs")
    private val converter = ConversionUtils()

    override fun getLatestGasReading(): Flow<GasReading?> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseDatabaseRepository", "Snapshot: $snapshot")
                Log.d("FirebaseDatabaseRepository", "Children: ${snapshot.children}")
                Log.d("FirebaseDatabaseRepository", "Value: ${snapshot.value}")

                for (data in snapshot.children) {
                    val readingMap = data.value as Map<String, Any>

                    Log.d("FirebaseDatabaseRepository", "Reading Map: $readingMap")
                    try {
                        // Get raw analog values
                        val rawCO = readingMap["CO"]?.toString()?.toInt() ?: 0
                        val rawBenzene = readingMap["Benzen"]?.toString()?.toInt() ?: 0  // Note: Fixed typo in key name
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
                        Log.d("FirebaseDatabaseRepository", "Reading after conversion: $reading")
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



    override suspend fun saveGasReading(reading: GasReading) {
        gasRef.push().setValue(reading).await()
    }

}