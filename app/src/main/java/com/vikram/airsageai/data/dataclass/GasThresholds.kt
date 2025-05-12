package com.vikram.airsageai.data.dataclass

data class GasThresholds(
    val max: Float,
    val warning: Float,
    val danger: Float
)

val gasThresholdsMap = mapOf(
    "CO" to GasThresholds(1000f, 500f, 800f),
    "Benzene" to GasThresholds(800f, 400f, 640f),
    "NH3" to GasThresholds(800f, 400f, 640f),
    "Smoke" to GasThresholds(700f, 350f, 560f),
    "LPG" to GasThresholds(1000f, 500f, 800f),
    "Methane" to GasThresholds(1000f, 500f, 800f),
    "Hydrogen" to GasThresholds(1000f, 500f, 800f)
)
