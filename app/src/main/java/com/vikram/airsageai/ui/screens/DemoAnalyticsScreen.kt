package com.vikram.airsageai.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.vikram.airsageai.data.dataclass.GasReading
import com.vikram.airsageai.ui.theme.AirSageAiTheme


class AnalyticsDemoActivity {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AnalyticsDemo() {
        // Generate dummy gas readings for preview/testing
        val dummyReadings = remember {
            generateDummyGasReadings(48, 30) // 48 readings, 30 minutes each
        }

        AirSageAiTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Air Quality Analytics",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    )
                }
            ) { paddingValues ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call the AnalyticsScreen composable with our dummy data
                    AnalyticsScreen(
                        paddingValues = paddingValues,
                        themeColor = MaterialTheme.colorScheme.background,
                        readings = dummyReadings
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsDemoPreview() {
    AnalyticsDemoActivity().AnalyticsDemo()
}

// Helper function to generate dummy readings - implementation from the DummyGasReadings artifact
fun generateDummyGasReadings(count: Int = 48, intervalMinutes: Int = 30): List<GasReading> {
    // This is a simplified version - use the full implementation from the DummyGasReadings artifact

    val readings = mutableListOf<GasReading>()

    // Create a set of dummy readings with realistic variations
    for (i in 0 until count) {
        val hourOfDay = (i * intervalMinutes / 60) % 24
        val isRushHour = hourOfDay in 7..9 || hourOfDay in 17..19

        // Base values with some time-based variations
        val co = 2.0 + if (isRushHour) 3.0 else 0.5
        val benzene = 0.02 + if (isRushHour) 0.03 else 0.01
        val nh3 = 0.5 + (i % 8) * 0.1
        val smoke = 40.0 + if (isRushHour) 30.0 else 10.0
        val lpg = 50.0 + (i % 10) * 5.0
        val ch4 = 300.0 + (i % 12) * 25.0
        val h2 = 50.0 + (i % 6) * 8.0

        readings.add(
            GasReading(
                CO = co,
                Benzene = benzene,
                NH3 = nh3,
                Smoke = smoke,
                LPG = lpg,
                CH4 = ch4,
                H2 = h2,
                Time = "2025-04-30 ${hourOfDay.toString().padStart(2, '0')}:${(i * intervalMinutes % 60).toString().padStart(2, '0')}:00"
            )
        )
    }

    return readings
}