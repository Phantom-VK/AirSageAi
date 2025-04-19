import kotlin.math.pow
import kotlin.math.log10

/**
 * Utility class for converting analog sensor readings to ppm values
 * for various gas sensors including MQ-5, MQ-7, and MQ-135
 */
class ConversionUtils {
    // Default calibration values for different sensors and gases
    companion object {
        // MQ-7 coefficients for CO
        val MQ7_CO_R0 = 10.0
        val MQ7_CO_A = -1.525
        val MQ7_CO_B = 1.994

        val MQ135_NH3_R0 = 102.2
        val MQ135_NH3_A = -0.41
        val MQ135_NH3_B = 0.234

        val MQ135_SMOKE_R0 = 77.0
        val MQ135_SMOKE_A = -0.361
        val MQ135_SMOKE_B = 0.711

        // MQ-5 coefficients
        val MQ5_LPG_R0 = 6.5
        val MQ5_LPG_A = -0.57
        val MQ5_LPG_B = 2.3

        val MQ5_CH4_R0 = 10.0
        val MQ5_CH4_A = -0.44
        val MQ5_CH4_B = 1.18

        val MQ5_H2_R0 = 21.24
        val MQ5_H2_A = -0.48
        val MQ5_H2_B = 0.93

        // Default values for circuit parameters
        const val DEFAULT_RL = 10_000.0  // Load resistance in ohms
        const val DEFAULT_VCC = 5.0      // Supply voltage, typically 5V or 3.3V

        // ADC resolution (Arduino uses 10-bit ADC: 2^10 = 1024)
        const val ADC_RESOLUTION = 1023.0
    }

    /**
     * Converts analog ADC value to voltage
     *
     * @param analogValue The raw analog reading (0-1023 for 10-bit ADC)
     * @param vcc The supply voltage (typically 5V or 3.3V)
     * @return The voltage in volts
     */
    fun analogToVoltage(analogValue: Int, vcc: Double = DEFAULT_VCC): Double {
        return analogValue * (vcc / ADC_RESOLUTION)
    }

    /**
     * Calculates sensor resistance (Rs) from output voltage
     *
     * @param vOut The output voltage measured across the load resistor
     * @param rL The load resistor value in ohms
     * @param vcc The supply voltage
     * @return The sensor resistance in ohms
     */
    fun voltageToRs(vOut: Double, rL: Double = DEFAULT_RL, vcc: Double = DEFAULT_VCC): Double {
        // Avoid division by zero
        if (vOut <= 0) return Double.MAX_VALUE

        return (vcc - vOut) * rL / vOut
    }

    /**
     * Calculates gas concentration in ppm using sensor resistance and calibration parameters
     *
     * @param rs The sensor resistance in ohms
     * @param r0 The sensor resistance in clean air
     * @param a The slope coefficient from datasheet curve
     * @param b The intercept coefficient from datasheet curve
     * @return The gas concentration in ppm
     */
    fun rsToPPM(rs: Double, r0: Double, a: Double, b: Double): Double {
        // Avoid division by zero
        if (r0 <= 0) return 0.0

        val ratio = rs / r0
        return 10.0.pow((a * log10(ratio)) + b)
    }

    /**
     * Converts analog value directly to ppm using a chain of conversions
     *
     * @param analogValue The raw analog reading (0-1023 for 10-bit ADC)
     * @param r0 The sensor resistance in clean air
     * @param a The slope coefficient from datasheet curve
     * @param b The intercept coefficient from datasheet curve
     * @param vcc The supply voltage
     * @param rL The load resistor value in ohms
     * @return The gas concentration in ppm
     */
    fun analogToPpm(
        analogValue: Int,
        r0: Double,
        a: Double,
        b: Double,
        vcc: Double = DEFAULT_VCC,
        rL: Double = DEFAULT_RL
    ): Double {
        val vOut = analogToVoltage(analogValue, vcc)
        val rs = voltageToRs(vOut, rL, vcc)
        return rsToPPM(rs, r0, a, b)
    }

    /**
     * Convenience method to convert MQ-7 analog reading to CO in ppm
     */
    fun convertCO(analogValue: Int): Double {
        return analogToPpm(analogValue, MQ7_CO_R0, MQ7_CO_A, MQ7_CO_B)
    }

    /**
     * Convenience method to convert MQ-135 analog reading to NH3 in ppm
     */
    fun convertNH3(analogValue: Int): Double {
        return analogToPpm(analogValue, MQ135_NH3_R0, MQ135_NH3_A, MQ135_NH3_B)
    }

    /**
     * Convenience method to convert MQ-135 analog reading to Smoke in ppm
     */
    fun convertSmoke(analogValue: Int): Double {
        return analogToPpm(analogValue, MQ135_SMOKE_R0, MQ135_SMOKE_A, MQ135_SMOKE_B)
    }

    /**
     * Convenience method to convert MQ-5 analog reading to LPG in ppm
     */
    fun convertLPG(analogValue: Int): Double {
        return analogToPpm(analogValue, MQ5_LPG_R0, MQ5_LPG_A, MQ5_LPG_B)
    }

    /**
     * Convenience method to convert MQ-5 analog reading to CH4 in ppm
     */
    fun convertCH4(analogValue: Int): Double {
        return analogToPpm(analogValue, MQ5_CH4_R0, MQ5_CH4_A, MQ5_CH4_B)
    }

    /**
     * Convenience method to convert MQ-5 analog reading to H2 in ppm
     */
    fun convertH2(analogValue: Int): Double {
        return analogToPpm(analogValue, MQ5_H2_R0, MQ5_H2_A, MQ5_H2_B)
    }

    /**
     * Convenience method to convert MQ-135 analog reading to Benzene in ppm
     * Note: Using rough estimation coefficients as MQ-135 is not optimized for Benzene
     */
    fun convertBenzene(analogValue: Int): Double {
        // Using approximate values - these should be calibrated for your specific sensor
        val benzeneR0 = 100.0
        val benzeneA = -0.42
        val benzeneB = 0.65

        return analogToPpm(analogValue, benzeneR0, benzeneA, benzeneB)
    }
}