package com.vikram.airsageai.utils

import com.vikram.airsageai.data.dataclass.GasReading
import com.vikram.airsageai.data.dataclass.GasReadingLists
import com.vikram.airsageai.data.dataclass.MinMax

class AnalyticsUtils {

    fun processGasReadings(gasReading: GasReading){

    }

    fun exportToExcel(data: List<GasReading>){

    }

    fun exportToCSV(data: List<GasReading>){

    }

    // Optimized function to extract gas readings
    fun extractGasReadings(readings: List<GasReading>): GasReadingLists {
        return readings.fold(GasReadingLists()) { acc, reading ->
            acc.apply {
                aqiReadings.add(reading.overallAQI().toDouble())
                coReadings.add(reading.CO?.toDouble() ?: 0.0)
                benzeneReadings.add(reading.Benzene?.toDouble() ?: 0.0)
                nh3Readings.add(reading.NH3?.toDouble() ?: 0.0)
                smokeReadings.add(reading.Smoke?.toDouble() ?: 0.0)
                lpgReadings.add(reading.LPG?.toDouble() ?: 0.0)
                ch4Readings.add(reading.CH4?.toDouble() ?: 0.0)
                h2Readings.add(reading.H2?.toDouble() ?: 0.0)
            }
        }
    }


    fun calculateMinMax(readings: List<GasReading>): MinMax {
        var min = Double.MAX_VALUE
        var max = Double.MIN_VALUE

        for (reading in readings) {
            val values = listOfNotNull(
                reading.CO,
                reading.Benzene,
                reading.NH3,
                reading.Smoke,
                reading.LPG,
                reading.CH4,
                reading.H2
            ).map { it.toDouble() }

            min = minOf(min, values.minOrNull() ?: Double.MAX_VALUE)
            max = maxOf(max, values.maxOrNull() ?: Double.MIN_VALUE)
        }

        return MinMax(min, max)
    }

}