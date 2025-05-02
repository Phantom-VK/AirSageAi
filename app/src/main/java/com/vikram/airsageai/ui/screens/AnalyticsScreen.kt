package com.vikram.airsageai.ui.screens

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.vikram.airsageai.data.dataclass.GasReading
import com.vikram.airsageai.utils.AnalyticsUtils
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line

@Composable
fun AnalyticsScreen(paddingValues: PaddingValues, themeColor: Color, readings:List<GasReading>) {

    val analyticsUtils = AnalyticsUtils()

    // Optimized gas reading extraction
    val (
        aqiReadings,
        coReadings,
        benzeneReadings,
        nh3Readings,
        smokeReadings,
        lpgReadings,
        ch4Readings,
        h2Readings
    ) = remember(readings) {
        analyticsUtils.extractGasReadings(readings)
    }

    val gasMinMax = remember(readings) { // Cache the result for the same 'readings'
        analyticsUtils.calculateMinMax(readings)
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(themeColor)
    ) {
            LineChart(
                modifier = Modifier
                    .padding(20.dp)
                    .weight(1f),
                data = remember {
                    listOf(
                        Line(
                            label = "AQI",
                            values = aqiReadings,
                            color = SolidColor(Color(0xFF2BC0A1)),
                            firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                            secondGradientFillColor = Color.Transparent,
                            strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                            gradientAnimationDelay = 1000,
                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                        )
                    )
                },
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 500L
                }),
                minValue = 0.0,
                maxValue = 500.0
            )

        LineChart(
            modifier = Modifier
                .padding(20.dp)
                .weight(1f),
            data = remember {
                listOf(
                    Line(
                        label = "CO",
                        values = coReadings,
                        color = SolidColor(Color(0xFFC0392B)),
                        curvedEdges = true
                    ),
                    Line(
                        label = "NH3",
                        values = nh3Readings,
                        color = SolidColor(Color(0xFF27AE60)),
                        curvedEdges = true
                    ),
                    Line(
                        label = "Benzene",
                        values = benzeneReadings,
                        color = SolidColor(Color(0xFF9B59B6)),
                        curvedEdges = true
                    ),
                    Line(
                        label = "Hydrogen",
                        values = h2Readings,
                        color = SolidColor(Color(0xFF3498DB)),
                        curvedEdges = true
                    ),
                    Line(
                        label = "Smoke",
                        values = smokeReadings,
                        color = SolidColor(Color(0xFF777777)),
                        curvedEdges = true
                    ),
                    Line(
                        label = "LPG",
                        values = lpgReadings,
                        color = SolidColor(Color(0xFFE67E22)),
                        curvedEdges = true
                    ),
                    Line(
                        label = "CH4",
                        values = ch4Readings,
                        color = SolidColor(Color(0xFFF1C40F)),
                        curvedEdges = true
                    ),
                )
            },
            animationMode = AnimationMode.Together(delayBuilder = {
                it * 500L
            }),
            minValue = gasMinMax.min,
            maxValue = gasMinMax.max
        )
    }

}


