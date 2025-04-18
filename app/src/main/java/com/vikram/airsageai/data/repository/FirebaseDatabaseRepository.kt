package com.vikram.airsageai.data.repository

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
    private val gasRef = database.getReference("test_data")

    override fun getLatestGasReading(): Flow<GasReading?> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val readingMap = data.value as? Map<String, Any>
                    if (readingMap != null) {
                        try {
                            val reading = GasReading(
                                CO_PPM = readingMap["CO (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                CO2_PPM = readingMap["CO2 (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                NH3_PPM = readingMap["NH3 (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                NOx_PPM = readingMap["NOx (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                LPG_PPM = readingMap["LPG (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                Methane_PPM = readingMap["Methane (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0,
                                Hydrogen_PPM = readingMap["Hydrogen (PPM)"]?.toString()?.toDoubleOrNull() ?: 0.0
                            )
                            trySend(reading)
                        } catch (e: Exception) {
                            trySend(null)
                        }
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