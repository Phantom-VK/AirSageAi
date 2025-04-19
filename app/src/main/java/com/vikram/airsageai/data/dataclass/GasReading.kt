package com.vikram.airsageai.data.dataclass

import com.google.firebase.database.PropertyName
import kotlin.math.roundToInt

data class GasReading(
    @get:PropertyName("CO")
    @set:PropertyName("CO")
    var CO: Double? = null,

    @get:PropertyName("Benzene")
    @set:PropertyName("Benzene ")
    var Benzene: Double? = null,

    @get:PropertyName("NH3")
    @set:PropertyName("NH3")
    var NH3: Double? = null,

    @get:PropertyName("Smoke")
    @set:PropertyName("Smoke")
    var Smoke: Double? = null,

    @get:PropertyName("LPG")
    @set:PropertyName("LPG")
    var LPG: Double? = null,

    @get:PropertyName("CH4")
    @set:PropertyName("CH4")
    var CH4: Double? = null,

    @get:PropertyName("H2")
    @set:PropertyName("H2")
    var H2: Double? = null,

    @get:PropertyName("Time")
    @set:PropertyName("Time")
    var Time: String? = null
) {
    /**
     * Converts all gas readings to their respective AQI values
     * @return Map of gas name to AQI value
     */
    fun toAQI(): Map<String, Int> {
        return mapOf(
            "CO" to convertCOToAQI(CO),
            "Benzene" to convertBenzeneToAQI(Benzene),
            "NH3" to convertNH3ToAQI(NH3),
            "Smoke" to convertSmokeToAQI(Smoke),
            "LPG" to convertLPGToAQI(LPG),
            "Methane" to convertMethaneToAQI(CH4),
            "Hydrogen" to convertHydrogenToAQI(H2)
        )
    }

    /**
     * Calculates overall AQI using the maximum of individual pollutant AQIs
     * This follows EPA's approach where the highest individual AQI determines overall air quality
     * @return The maximum AQI value among all pollutants
     */
    fun overallAQI(): Int {
        return toAQI().values.maxOrNull() ?: 0
    }

    /**
     * Calculate AQI using linear interpolation between breakpoints
     * This gives more accurate AQI values than simple ranges
     */
    private fun calculateAQI(
        concentration: Double,
        breakpoints: List<Double>,
        aqiValues: List<Int>
    ): Int {
        // Handle edge cases
        if (concentration <= breakpoints.first()) return aqiValues.first()
        if (concentration >= breakpoints.last()) return aqiValues.last()

        // Find which breakpoint range the concentration falls into
        for (i in 0 until breakpoints.size - 1) {
            if (concentration <= breakpoints[i + 1]) {
                // Linear interpolation formula:
                // AQI = ((AQI_high - AQI_low) / (Conc_high - Conc_low)) * (Conc - Conc_low) + AQI_low
                val aqiLow = aqiValues[i]
                val aqiHigh = aqiValues[i + 1]
                val concLow = breakpoints[i]
                val concHigh = breakpoints[i + 1]

                return ((aqiHigh - aqiLow) * (concentration - concLow) / (concHigh - concLow) + aqiLow).roundToInt()
            }
        }
        return aqiValues.last() // Fallback
    }

    /**
     * Convert CO concentration (ppm) to AQI
     * Based on EPA CO breakpoints: https://www.airnow.gov/sites/default/files/2020-05/aqi-technical-assistance-document-sept2018.pdf
     */
    fun convertCOToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0

        // CO breakpoints in ppm
        val breakpoints = listOf(0.0, 4.4, 9.4, 12.4, 15.4, 30.4, 40.4, 50.4)
        // Corresponding AQI values
        val aqiValues = listOf(0, 50, 100, 150, 200, 300, 400, 500)

        return calculateAQI(value, breakpoints, aqiValues)
    }

    /**
     * Convert Benzene concentration (ppm) to AQI
     * Note: EPA doesn't have official AQI for Benzene, using health-based thresholds
     */
    fun convertBenzeneToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0

        // Benzene breakpoints in ppm (based on health guidelines)
        val breakpoints = listOf(0.0, 0.005, 0.05, 0.3, 1.0, 3.0, 5.0, 10.0)
        val aqiValues = listOf(0, 50, 100, 150, 200, 300, 400, 500)

        return calculateAQI(value, breakpoints, aqiValues)
    }

    /**
     * Convert Smoke (particulate matter proxy) to AQI
     * Using PM2.5-like scale as "Smoke" is a general term
     */
    fun convertSmokeToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0

        // Smoke proxy breakpoints
        val breakpoints = listOf(0.0, 50.0, 150.0, 350.0, 600.0, 1000.0, 1500.0, 2000.0)
        val aqiValues = listOf(0, 50, 100, 150, 200, 300, 400, 500)

        return calculateAQI(value, breakpoints, aqiValues)
    }

    /**
     * Convert NH3 (Ammonia) concentration to AQI
     * Based on health-based exposure guidelines
     */
    fun convertNH3ToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0

        // NH3 breakpoints in ppm
        val breakpoints = listOf(0.0, 0.2, 1.0, 5.0, 15.0, 30.0, 40.0, 50.0)
        val aqiValues = listOf(0, 50, 100, 150, 200, 300, 400, 500)

        return calculateAQI(value, breakpoints, aqiValues)
    }

    /**
     * Convert LPG concentration to AQI
     * Based on safety thresholds (LEL - Lower Explosive Limit fractions)
     */
    fun convertLPGToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0

        // LPG breakpoints in ppm
        val breakpoints = listOf(0.0, 100.0, 300.0, 800.0, 1500.0, 3000.0, 6000.0, 10000.0)
        val aqiValues = listOf(0, 50, 100, 150, 200, 300, 400, 500)

        return calculateAQI(value, breakpoints, aqiValues)
    }

    /**
     * Convert Methane (CH4) concentration to AQI
     * Based on safety thresholds (methane becomes hazardous at higher levels)
     */
    fun convertMethaneToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0

        // Methane breakpoints in ppm
        val breakpoints = listOf(0.0, 500.0, 1000.0, 3000.0, 5000.0, 10000.0, 15000.0, 20000.0)
        val aqiValues = listOf(0, 50, 100, 150, 200, 300, 400, 500)

        return calculateAQI(value, breakpoints, aqiValues)
    }

    /**
     * Convert Hydrogen (H2) concentration to AQI
     * Based on safety thresholds (H2 lower explosive limit is around 40,000 ppm)
     */
    fun convertHydrogenToAQI(ppm: Double?): Int {
        val value = ppm ?: return 0

        // Hydrogen breakpoints in ppm
        val breakpoints = listOf(0.0, 100.0, 400.0, 800.0, 1500.0, 3000.0, 6000.0, 10000.0)
        val aqiValues = listOf(0, 50, 100, 150, 200, 300, 400, 500)

        return calculateAQI(value, breakpoints, aqiValues)
    }

    /**
     * Get the AQI category description based on AQI value
     * Following EPA's AQI categories
     */
    fun getAQICategory(aqi: Int): String {
        return when (aqi) {
            in 0..50 -> "Good"
            in 51..100 -> "Moderate"
            in 101..150 -> "Unhealthy for Sensitive Groups"
            in 151..200 -> "Unhealthy"
            in 201..300 -> "Very Unhealthy"
            in 301..500 -> "Hazardous"
            else -> "Out of Range"
        }
    }

    /**
     * Get color code for the AQI value for UI display
     */
    fun getAQIColor(aqi: Int): String {
        return when (aqi) {
            in 0..50 -> "#00E400" // Green
            in 51..100 -> "#FFFF00" // Yellow
            in 101..150 -> "#FF7E00" // Orange
            in 151..200 -> "#FF0000" // Red
            in 201..300 -> "#8F3F97" // Purple
            else -> "#7E0023" // Maroon
        }
    }

    /**
     * Get health implications for the given AQI
     */
    fun getHealthImplications(aqi: Int): String {
        return when (aqi) {
            in 0..50 -> "Air quality is satisfactory, and air pollution poses little or no risk."
            in 51..100 -> "Air quality is acceptable. However, some pollutants may be a concern for a very small number of people who are unusually sensitive to air pollution."
            in 101..150 -> "Members of sensitive groups may experience health effects. The general public is less likely to be affected."
            in 151..200 -> "Some members of the general public may experience health effects; members of sensitive groups may experience more serious health effects."
            in 201..300 -> "Health alert: The risk of health effects is increased for everyone."
            else -> "Health warning of emergency conditions: everyone is more likely to be affected."
        }
    }
}