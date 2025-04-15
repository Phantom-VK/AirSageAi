package com.vikram.airsageai.data.dataclass

data class GasState(
    val gasValue: Float,
    val maxGasValue: Float,
    val warningThreshold: Float,
    val dangerThreshold: Float
)
