package com.vikram.airsageai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikram.airsageai.R
import com.vikram.airsageai.ui.components.CircularSpeedIndicator
import com.vikram.airsageai.utils.GasReading
import com.vikram.airsageai.utils.GasState

@Composable
fun HomeScreen(paddingValues: PaddingValues,
               latestReading: GasReading?,
               aqiValues: Map<String, Int>? = null,
               overallAQI: Int? = null,
               themeColor: Color) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(themeColor)
    ) {
        items(1) {
            AQIDisplay(overallAQI)
            Spacer(modifier = Modifier.height(16.dp))
            ObservationsGrid(latestReading)
        }
    }
}


@Composable
fun TopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.size(24.dp)
        )

        // Location with dropdown
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Vishnupuri, Nanded",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select location"
            )
        }

        // Date display
        Text(
            text = "5 April 2025",
            fontSize = 14.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun AQIDisplay(aqi: Int?) {

    var image: Painter = painterResource(id = R.drawable.good_weather)

    image = when (aqi) {
        in 0..50 -> painterResource(id = R.drawable.good_weather)
        in 51..100 -> painterResource(id = R.drawable.bad_weather)
        else -> painterResource(id = R.drawable.worst_weather)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {

        // This would be your actual background scenery image
        // Replace R.drawable.landscape_background with your actual drawable

        Image(
            painter = image,
            contentDescription = "Landscape Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )


        // AQI Value and Label
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            TopAppBar()
            Text(
                text = aqi.toString(),
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )


            Text(
                text = "Air Quality Index",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ObservationsGrid(
    gasReading: GasReading?
) {
    val CO = gasReading?.CO_PPM?.toFloat() ?: 0f
    val CO2 = gasReading?.CO2_PPM?.toFloat() ?: 0f
    val NH3 = gasReading?.NH3_PPM?.toFloat() ?: 0f
    val NOx = gasReading?.NOx_PPM?.toFloat() ?: 0f
    val LPG = gasReading?.LPG_PPM?.toFloat() ?: 0f
    val Methane = gasReading?.Methane_PPM?.toFloat() ?: 0f
    val Hydrogen = gasReading?.Hydrogen_PPM?.toFloat() ?: 0f


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Current Observations",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ObservationCard("CO", CO, modifier = Modifier.weight(1f))
            ObservationCard("CO2", CO2, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ObservationCard("NH3", NH3, modifier = Modifier.weight(1f))
            ObservationCard("NOx", NOx, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Third row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ObservationCard("LPG", LPG, modifier = Modifier.weight(1f))
            ObservationCard("Methane", Methane, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fourth row - Single card centered
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ObservationCard("Hydrogen", gasValue = Hydrogen, modifier = Modifier.width(180.dp))
        }
    }
}

@Composable
fun ObservationCard(
    title: String,
    gasValue: Float,
    maxGasValue: Float = 1000f,
    warningThreshold: Float = 500f,
    dangerThreshold: Float = 800f,
    unit: String = "ppm",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Speed Indicator replacing the GasPpmGauge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(50))
                ) {
                    CircularSpeedIndicator(
                        gasState = GasState(
                            gasValue = gasValue,
                            maxGasValue = maxGasValue,
                            warningThreshold = warningThreshold,
                            dangerThreshold = dangerThreshold
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Chemical name
                Text(
                    text = title,
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Value
            Text(
                text = "$gasValue $unit",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

