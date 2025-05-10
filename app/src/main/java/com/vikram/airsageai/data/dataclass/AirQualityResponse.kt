package com.vikram.airsageai.data.dataclass

data class AirQualityResponse(
    val indexes: List<AqiIndex>
)

data class AqiIndex(
    val code: String,
    val displayName: String,
    val aqi: Int,
    val aqiDisplay: String,
    val category: String,
    val dominantPollutant: String
)
