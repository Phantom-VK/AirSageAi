package com.vikram.airsageai.viewmodels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.vikram.airsageai.utils.GasReading

class GasDataViewModel : ViewModel() {

    private val database = Firebase.database
    private val gasRef = database.getReference("test_data")

    // For latest reading only
    private var _latestReading = mutableStateOf<GasReading?>(null)
    val latestReading: State<GasReading?> = _latestReading

    init {
//        gasRef.removeValue()
        fetchLatestGasReading()
    }

    private fun fetchLatestGasReading() {
        gasRef
            .limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Since it's an array, snapshot.children is an iterable of entries
                    for (data in snapshot.children) {
                        val readingMap = data.value as? Map<String, Any>
                        Log.d("Firebase", "Received data: $readingMap")
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
                                _latestReading.value = reading
                                Log.d("Firebase", "Parsed reading: $reading")
                            } catch (e: Exception) {
                                Log.e("Firebase", "Parsing failed", e)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Data fetch cancelled", error.toException())
                }
            })
    }


}
