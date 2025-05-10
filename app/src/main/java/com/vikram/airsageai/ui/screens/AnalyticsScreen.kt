package com.vikram.airsageai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikram.airsageai.data.dataclass.GasReading
import com.vikram.airsageai.utils.AnalyticsUtils
import com.vikram.airsageai.utils.exportToCSV
import com.vikram.airsageai.utils.exportToExcel
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.Line

@Composable
fun AnalyticsScreen(paddingValues: PaddingValues, themeColor: Color, readings:List<GasReading>) {
    val context = LocalContext.current

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
            .background(themeColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Last 7 Days Readings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )



        HorizontalDivider(
            modifier = Modifier
                .padding(20.dp),
            2.dp, Color.Black
        )

        LineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(20.dp),
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
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Button(
                onClick = {
                   context.exportToExcel(readings)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)

            ) {
                Text("Export to Excel", color = Color.White)
            }

            Button(
                onClick = {
                    context.exportToCSV(readings)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)

            ) {
                Text("Export to CSV", color = Color.White)
            }
        }


    }

}


