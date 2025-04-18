import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vikram.airsageai.utils.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel(
    private val locationUtils: LocationUtils,
    private val context: Context
) : ViewModel() {
    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    private val _locationName = MutableStateFlow<String>("Loading location...")
    val locationName: StateFlow<String> = _locationName

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchLocation() {
        viewModelScope.launch {
            try {
                val newLocation = locationUtils.getCurrentLocation()
                _location.value = newLocation

                // Get location name if we have a valid location
                newLocation?.let { location ->
                    _locationName.value = locationUtils.getLocationName(
                        context,
                        location.latitude,
                        location.longitude
                    )
                }

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to get location: ${e.message}"
            }
        }
    }
}

class LocationViewModelFactory(
    private val locationUtils: LocationUtils,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(locationUtils, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}