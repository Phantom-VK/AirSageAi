// DatabaseRepository.kt
package com.vikram.airsageai.data.repository

import com.vikram.airsageai.data.dataclass.GasReading
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {
    fun getLatestGasReading(): Flow<GasReading?>
    fun getLast7DaysReadings(): Flow<List<GasReading>>
}