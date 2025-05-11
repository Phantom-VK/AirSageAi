package com.vikram.airsageai.data.dataclass

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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

@JsonClass(generateAdapter = true)
data class Location(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)
