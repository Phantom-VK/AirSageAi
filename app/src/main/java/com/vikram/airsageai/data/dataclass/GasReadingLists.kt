package com.vikram.airsageai.data.dataclass

data class GasReadingLists(
    val aqiReadings: MutableList<Double> = mutableListOf(),
    val coReadings: MutableList<Double> = mutableListOf(),
    val benzeneReadings: MutableList<Double> = mutableListOf(),
    val nh3Readings: MutableList<Double> = mutableListOf(),
    val smokeReadings: MutableList<Double> = mutableListOf(),
    val lpgReadings: MutableList<Double> = mutableListOf(),
    val ch4Readings: MutableList<Double> = mutableListOf(),
    val h2Readings: MutableList<Double> = mutableListOf()
)