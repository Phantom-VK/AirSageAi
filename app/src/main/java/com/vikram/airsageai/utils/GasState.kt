package com.vikram.airsageai.utils

data class GasState(
    val gasValue: Float,
    val maxGasValue: Float,
    val warningThreshold: Float,
    val dangerThreshold: Float
)
