package com.vikram.airsageai.data.dataclass

data class AirQualityRequest(
    val universalAqi: Boolean = true,
    val location: Location,
    val extraComputations: List<String> = listOf(
        "HEALTH_RECOMMENDATIONS",
        "DOMINANT_POLLUTANT_CONCENTRATION",
        "POLLUTANT_CONCENTRATION",
        "LOCAL_AQI",
        "POLLUTANT_ADDITIONAL_INFO"
    ),
    val languageCode: String = "en"
)

data class Location(
    val latitude: Double,
    val longitude: Double
)
