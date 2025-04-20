package com.vikram.airsageai.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikram.airsageai.data.dataclass.GasReading
import com.vikram.airsageai.data.repository.FirebaseDatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GasDataViewModel @Inject constructor(
    private val repository: FirebaseDatabaseRepository
) : ViewModel() {
    // Data state for latest reading with loading and error states
    private val _latestReadingState = MutableStateFlow<DataState<GasReading?>>(DataState.Loading)
    val latestReadingState: StateFlow<DataState<GasReading?>> = _latestReadingState

    // Data state for weekly readings with loading and error states
    private val _weeklyReadingState = MutableStateFlow<DataState<List<GasReading>>>(DataState.Loading)
    val weeklyReadingState: StateFlow<DataState<List<GasReading>>> = _weeklyReadingState

    // General error state for operations like saving data
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchLatestGasReading()
        fetchWeeklyReadings()
    }

    private fun fetchLatestGasReading() {
        viewModelScope.launch {
            // Set loading state
            _latestReadingState.value = DataState.Loading

            try {
                repository.getLatestGasReading()
                    .collectLatest { reading ->
                        // Update with success state containing the data
                        _latestReadingState.value = DataState.Success(reading)
                    }
            } catch (e: Exception) {
                // Update with error state containing the error message
                _latestReadingState.value = DataState.Error("Failed to fetch latest reading: ${e.message}")
            }
        }
    }

    private fun fetchWeeklyReadings() {
        viewModelScope.launch {
            // Set loading state
            _weeklyReadingState.value = DataState.Loading

            try {
                repository.getLast7DaysReadings()
                    .collectLatest { readings ->
                        // Update with success state containing the data
                        _weeklyReadingState.value = DataState.Success(readings)
                    }
            } catch (e: Exception) {
                // Update with error state containing the error message
                _weeklyReadingState.value = DataState.Error("Failed to fetch weekly readings: ${e.message}")
            }
        }
    }

    // Retry functions in case of errors
    fun retryLatestReading() {
        fetchLatestGasReading()
    }

    fun retryWeeklyReadings() {
        fetchWeeklyReadings()
    }

    suspend fun saveReading(reading: GasReading) {
        try {
            repository.saveGasReading(reading)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to save reading: ${e.message}"
        }
    }

    // Sealed class to represent different states of data loading
    sealed class DataState<out T> {
        object Loading : DataState<Nothing>()
        data class Success<T>(val data: T) : DataState<T>()
        data class Error(val message: String) : DataState<Nothing>()
    }
}