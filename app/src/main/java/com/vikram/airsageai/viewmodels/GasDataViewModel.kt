// GasDataViewModel.kt
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
    private val _latestReading = MutableStateFlow<GasReading?>(null)
    val latestReading: StateFlow<GasReading?> = _latestReading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchLatestGasReading()
    }

    private fun fetchLatestGasReading() {
        viewModelScope.launch {
            repository.getLatestGasReading()
                .collectLatest { reading ->
                    _latestReading.value = reading
                }
        }
    }

    suspend fun saveReading(reading: GasReading) {
        try {
            repository.saveGasReading(reading)
        } catch (e: Exception) {
            _error.value = "Failed to save reading: ${e.message}"
        }
    }
}