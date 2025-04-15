package com.vikram.airsageai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import com.vikram.airsageai.data.dataclass.GasState

@Composable
fun CircularSpeedIndicator(
    gasState: GasState,
    modifier: Modifier = Modifier
) {
    // Calculate progress value between 0 and 1
    val progress = (gasState.gasValue / gasState.maxGasValue).coerceIn(0f, 1f)
    val angle = 240f // Fixed angle for the arc

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.5f
        val strokeWidth = size.minDimension / 10f

        // Background track
        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = 150f,
            sweepAngle = angle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Good zone (green)
        val warningAngle = angle * (gasState.warningThreshold / gasState.maxGasValue)
        drawArc(
            color = Color.Green.copy(alpha = 0.7f),
            startAngle = 150f,
            sweepAngle = warningAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Warning zone (yellow/orange)
        val dangerStartAngle = 150f + warningAngle
        val warningToleranceAngle = angle * ((gasState.dangerThreshold - gasState.warningThreshold) / gasState.maxGasValue)
        drawArc(
            color = Color(0xFFFFA500).copy(alpha = 0.7f), // Orange
            startAngle = dangerStartAngle,
            sweepAngle = warningToleranceAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Danger zone (red)
        val dangerStartAngleActual = dangerStartAngle + warningToleranceAngle
        val dangerSweepAngle = angle * ((gasState.maxGasValue - gasState.dangerThreshold) / gasState.maxGasValue)
        drawArc(
            color = Color.Red.copy(alpha = 0.7f),
            startAngle = dangerStartAngleActual,
            sweepAngle = dangerSweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress indicator (needle)
        val progressSweepAngle = angle * progress
        val needleAngle = 150f + progressSweepAngle
        val needleLength = radius * 0.8f

        // Calculate needle endpoint using trigonometry
        val needleEndX = center.x + (needleLength * Math.cos(Math.toRadians(needleAngle.toDouble()))).toFloat()
        val needleEndY = center.y + (needleLength * Math.sin(Math.toRadians(needleAngle.toDouble()))).toFloat()

        // Draw needle
        drawLine(
            color = Color.Black,
            start = center,
            end = Offset(needleEndX, needleEndY),
            strokeWidth = strokeWidth / 2,
            cap = StrokeCap.Round
        )

        // Draw needle center
        drawCircle(
            color = getStatusColor(gasState),
            radius = strokeWidth / 2,
            center = center
        )
    }
}


private fun getStatusColor(gasState: GasState): Color {
    return when {
        gasState.gasValue >= gasState.dangerThreshold -> Color.Red
        gasState.gasValue >= gasState.warningThreshold -> Color(0xFFFFA500) // Orange
        else -> Color.Green
    }
}



@Preview(showBackground = true)
@Composable
fun SpeedoMeterPreview() {
    val gasState = GasState(
        gasValue = 650f,
        maxGasValue = 1000f,
        warningThreshold = 500f,
        dangerThreshold = 800f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        CircularSpeedIndicator(
            gasState = gasState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}