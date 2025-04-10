package com.vikram.airsageai.utils

import com.google.firebase.database.PropertyName
import kotlin.math.roundToInt


data class GasReading(
    @get:PropertyName("CO (PPM)")
    @set:PropertyName("CO (PPM)")
    var CO_PPM: Double? = null,

    @get:PropertyName("CO2 (PPM)")
    @set:PropertyName("CO2 (PPM)")
    var CO2_PPM: Double? = null,

    @get:PropertyName("NH3 (PPM)")
    @set:PropertyName("NH3 (PPM)")
    var NH3_PPM: Double? = null,

    @get:PropertyName("NOx (PPM)")
    @set:PropertyName("NOx (PPM)")
    var NOx_PPM: Double? = null,

    @get:PropertyName("LPG (PPM)")
    @set:PropertyName("LPG (PPM)")
    var LPG_PPM: Double? = null,

    @get:PropertyName("Methane (PPM)")
    @set:PropertyName("Methane (PPM)")
    var Methane_PPM: Double? = null,

    @get:PropertyName("Hydrogen (PPM)")
    @set:PropertyName("Hydrogen (PPM)")
    var Hydrogen_PPM: Double? = null
){
    // Extension function to convert all readings to AQI
    fun toAQI(): Map<String, Int> {
        return mapOf(
            "CO" to convertCOToAQI(CO_PPM),
            "CO2" to convertCO2ToAQI(CO2_PPM),
            "NH3" to convertNH3ToAQI(NH3_PPM),
            "NOx" to convertNOxToAQI(NOx_PPM),
            "LPG" to convertLPGToAQI(LPG_PPM),
            "Methane" to convertMethaneToAQI(Methane_PPM),
            "Hydrogen" to convertHydrogenToAQI(Hydrogen_PPM)
        ).filterValues { true }.mapValues { it.value }
    }

    // Calculate overall AQI (using the maximum of individual AQIs)
    fun overallAQI(): Int {
        return toAQI().values.maxOrNull() ?: 0
    }

    fun convertCOToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0
        return when {
            value <= 9 -> 50     // Good
            value <= 35 -> 100   // Moderate
            value <= 50 -> 150   // Unhealthy for sensitive
            value <= 150 -> 200  // Unhealthy
            else -> 300          // Hazardous
        }
    }

    fun convertCO2ToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0
        return when {
            value <= 1000 -> 50
            value <= 2000 -> 100
            value <= 5000 -> 150
            else -> 200
        }
    }

    fun convertNH3ToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0
        return when {
            value <= 1 -> 50
            value <= 5 -> 100
            value <= 10 -> 150
            else -> 200
        }
    }

    fun convertNOxToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0
        return when {
            value <= 0.05 -> 50
            value <= 0.1 -> 100
            value <= 0.2 -> 150
            value <= 0.4 -> 200
            else -> 300
        }
    }

    fun convertLPGToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0
        return when {
            value <= 100 -> 50
            value <= 500 -> 100
            value <= 1000 -> 150
            else -> 250
        }
    }

    fun convertMethaneToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0
        return when {
            value <= 1000 -> 50
            value <= 3000 -> 100
            value <= 5000 -> 150
            else -> 200
        }
    }

    fun convertHydrogenToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0
        return when {
            value <= 100 -> 50
            value <= 500 -> 100
            value <= 1000 -> 150
            else -> 200
        }
    }


    // Generic AQI calculation function
    private fun calculateAQI(
        concentration: Double,
        breakpoints: List<Double>,
        aqiBreakpoints: List<Int>
    ): Int {
        require(breakpoints.size == aqiBreakpoints.size - 1) {
            "Breakpoints and AQI breakpoints must match"
        }

        // Find the appropriate range
        for (i in 0 until breakpoints.size - 1) {
            if (concentration >= breakpoints[i] && concentration <= breakpoints[i + 1]) {
                return linearInterpolation(
                    concentration = concentration,
                    cLow = breakpoints[i],
                    cHigh = breakpoints[i + 1],
                    iLow = aqiBreakpoints[i],
                    iHigh = aqiBreakpoints[i + 1]
                ).roundToInt()
            }
        }

        // If concentration is above the highest breakpoint, return max AQI
        return aqiBreakpoints.last()
    }

    private fun linearInterpolation(
        concentration: Double,
        cLow: Double,
        cHigh: Double,
        iLow: Int,
        iHigh: Int
    ): Double {
        return ((iHigh - iLow) / (cHigh - cLow)) * (concentration - cLow) + iLow
    }
}




