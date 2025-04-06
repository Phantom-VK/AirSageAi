package com.vikram.airsageai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GasPpmGauge(
    currentValue: Float,
    minValue: Float = 0f,
    maxValue: Float = 1000f,
    modifier: Modifier = Modifier,
    gaugeSize: Dp = 50.dp,
    warningThreshold: Float = 500f,
    dangerThreshold: Float = 800f
) {
    // Validate inputs
    require(minValue < maxValue) { "minValue must be less than maxValue" }
    require(currentValue in minValue..maxValue) { "currentValue must be between minValue and maxValue" }
    require(warningThreshold < dangerThreshold) { "warningThreshold must be less than dangerThreshold" }
    
    // Calculate value percentage for color determination
    val valuePercentage = (currentValue - minValue) / (maxValue - minValue)
    val gaugeColor = when {
        currentValue >= dangerThreshold -> Color(0xFFFF5252) // Material red A200
        currentValue >= warningThreshold -> Color(0xFFFFD740) // Material amber A200
        else -> Color(0xFF69F0AE) // Material green A200
    }

    Box(
        modifier = modifier
            .size(gaugeSize),
        contentAlignment = Alignment.Center
    ) {
        // Draw the gauge background and needle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.width * 0.1f // Thinner stroke for small gauge
            
            drawModernGaugeBackground(
                minValue = minValue,
                maxValue = maxValue,
                warningThreshold = warningThreshold,
                dangerThreshold = dangerThreshold,
                strokeWidth = strokeWidth
            )
            
            drawModernNeedle(
                currentValue = currentValue,
                minValue = minValue,
                maxValue = maxValue,
                needleColor = gaugeColor
            )
        }

        // Display current value in the center
        Text(
            text = formatValue(currentValue),
            color = gaugeColor,
            fontSize = (gaugeSize.value * 0.22).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatValue(value: Float): String {
    return when {
        value >= 1000 -> "%.1fk".format(value / 1000)
        value >= 100 -> "%.0f".format(value)
        else -> "%.1f".format(value)
    }
}

private fun DrawScope.drawModernGaugeBackground(
    minValue: Float,
    maxValue: Float,
    warningThreshold: Float,
    dangerThreshold: Float,
    strokeWidth: Float
) {
    val sweepAngle = 270f // Wider sweep angle for better readability
    val startAngle = 135f // Starting at upper left
    
    // Calculate the center and radius
    val center = Offset(size.width / 2, size.height / 2)
    val outerRadius = (size.width / 2) - (strokeWidth / 2)
    
    // Background track with rounded edges
    drawArc(
        color = Color(0x1F000000), // Semi-transparent black
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // Draw the colored segments with gradient effect
    val safeEndValue = minValue + (warningThreshold - minValue).coerceAtLeast(0f)
    val warningEndValue = minValue + (dangerThreshold - minValue).coerceAtLeast(0f)
    
    // Safe zone (green)
    if (safeEndValue > minValue) {
        val safeSweep = (safeEndValue - minValue) / (maxValue - minValue) * sweepAngle
        drawGradientArc(
            startAngle = startAngle,
            sweepAngle = safeSweep,
            center = center,
            radius = outerRadius,
            startColor = Color(0xFF66BB6A), // Green 400
            endColor = Color(0xFF81C784), // Green 300
            strokeWidth = strokeWidth
        )
    }
    
    // Warning zone (yellow)
    if (warningEndValue > safeEndValue) {
        val warningSweep = (warningEndValue - safeEndValue) / (maxValue - minValue) * sweepAngle
        drawGradientArc(
            startAngle = startAngle + ((safeEndValue - minValue) / (maxValue - minValue) * sweepAngle),
            sweepAngle = warningSweep,
            center = center,
            radius = outerRadius,
            startColor = Color(0xFFFBC02D), // Yellow 700
            endColor = Color(0xFFFFD54F), // Yellow 300
            strokeWidth = strokeWidth
        )
    }
    
    // Danger zone (red)
    if (maxValue > warningEndValue) {
        val dangerSweep = (maxValue - warningEndValue) / (maxValue - minValue) * sweepAngle
        drawGradientArc(
            startAngle = startAngle + ((warningEndValue - minValue) / (maxValue - minValue) * sweepAngle),
            sweepAngle = dangerSweep,
            center = center,
            radius = outerRadius,
            startColor = Color(0xFFE57373), // Red 300
            endColor = Color(0xFFF44336), // Red 500
            strokeWidth = strokeWidth
        )
    }
    
    // Draw minimal tick marks for small gauge
    drawMinimalTicks(center, outerRadius, startAngle, sweepAngle, strokeWidth)
}

private fun DrawScope.drawGradientArc(
    startAngle: Float,
    sweepAngle: Float,
    center: Offset,
    radius: Float,
    startColor: Color,
    endColor: Color,
    strokeWidth: Float
) {
    // Create a gradient brush
    val brush = Brush.linearGradient(
        colors = listOf(startColor, endColor),
        start = Offset(
            center.x + radius * cos(Math.toRadians((startAngle).toDouble())).toFloat(),
            center.y + radius * sin(Math.toRadians((startAngle).toDouble())).toFloat()
        ),
        end = Offset(
            center.x + radius * cos(Math.toRadians((startAngle + sweepAngle).toDouble())).toFloat(),
            center.y + radius * sin(Math.toRadians((startAngle + sweepAngle).toDouble())).toFloat()
        )
    )
    
    drawArc(
        brush = brush,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawMinimalTicks(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    strokeWidth: Float
) {
    val majorTickCount = 3 // Reduced for small gauge
    val tickLength = strokeWidth * 0.8f
    
    // Draw just a few major ticks
    for (i in 0..majorTickCount) {
        val angle = startAngle + (sweepAngle * i / majorTickCount)
        val outerRadius = radius + (strokeWidth / 2)
        val innerRadius = outerRadius + tickLength
        
        val startPos = Offset(
            center.x + outerRadius * cos(Math.toRadians(angle.toDouble())).toFloat(),
            center.y + outerRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
        )
        val endPos = Offset(
            center.x + innerRadius * cos(Math.toRadians(angle.toDouble())).toFloat(),
            center.y + innerRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
        )
        
        drawLine(
            color = Color(0x99000000), // Semi-transparent black
            start = startPos,
            end = endPos,
            strokeWidth = strokeWidth * 0.2f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawModernNeedle(
    currentValue: Float,
    minValue: Float,
    maxValue: Float,
    needleColor: Color
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.width * 0.38f // Slightly shorter than gauge
    val startAngle = 135f
    val sweepAngle = 270f
    
    // Calculate needle angle
    val valueRatio = (currentValue - minValue) / (maxValue - minValue)
    val needleAngle = startAngle + sweepAngle * valueRatio
    
    // Needle parameters
    val needleBaseWidth = size.width * 0.03f
    val needleTipWidth = size.width * 0.01f
    val needleCapRadius = size.width * 0.08f
    
    // Draw needle with shadow for depth
    rotate(needleAngle, center) {
        // Needle shadow
        val shadowPath = Path().apply {
            moveTo(center.x - needleBaseWidth / 2, center.y)
            lineTo(center.x + needleBaseWidth / 2, center.y)
            lineTo(center.x, center.y - radius)
            close()
        }
        drawPath(
            path = shadowPath,
            color = Color(0x33000000), // Shadow
            style = Fill,
            blendMode = BlendMode.SrcOver
        )
        
        // Actual needle
        val needlePath = Path().apply {
            moveTo(center.x - needleBaseWidth / 2, center.y)
            lineTo(center.x + needleBaseWidth / 2, center.y)
            lineTo(center.x, center.y - radius)
            close()
        }
        drawPath(
            path = needlePath,
            color = needleColor,
            style = Fill,
            blendMode = BlendMode.SrcOver
        )
    }
    
    // Draw needle cap with gradient for 3D effect
    val capGradient = Brush.radialGradient(
        colors = listOf(Color.White, needleColor.copy(alpha = 0.8f)),
        center = Offset(center.x - needleCapRadius * 0.2f, center.y - needleCapRadius * 0.2f),
        radius = needleCapRadius * 1.2f
    )
    
    drawCircle(
        brush = capGradient,
        radius = needleCapRadius,
        center = center
    )
    
    // Highlight on cap for metallic look
    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = needleCapRadius * 0.4f,
        center = Offset(center.x - needleCapRadius * 0.2f, center.y - needleCapRadius * 0.2f)
    )
}

@Preview(showBackground = true)
@Composable
fun GasPpmGaugePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Low value
        GasPpmGauge(
            currentValue = 300f,
            minValue = 0f,
            maxValue = 1000f,
            warningThreshold = 500f,
            dangerThreshold = 800f,
            gaugeSize = 50.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Medium value
        GasPpmGauge(
            currentValue = 650f,
            minValue = 0f,
            maxValue = 1000f,
            warningThreshold = 500f,
            dangerThreshold = 800f,
            gaugeSize = 50.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // High value
        GasPpmGauge(
            currentValue = 850f,
            minValue = 0f,
            maxValue = 1000f,
            warningThreshold = 500f,
            dangerThreshold = 800f,
            gaugeSize = 50.dp
        )
    }
}