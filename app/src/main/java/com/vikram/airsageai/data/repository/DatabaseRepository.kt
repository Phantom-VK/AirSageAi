// DatabaseRepository.kt
package com.vikram.airsageai.data.repository

import com.vikram.airsageai.data.dataclass.GasReading
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {
    fun getLatestGasReading(): Flow<GasReading?>
    suspend fun saveGasReading(reading: GasReading)
}