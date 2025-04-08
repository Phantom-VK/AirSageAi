package com.vikram.airsageai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vikram.airsageai.R
import com.vikram.airsageai.ui.components.AppBottomBar
import com.vikram.airsageai.ui.components.GasPpmGauge

@Composable
fun HomeScreen(navController: NavController){
    AirQualityIndexScreen()
}

@Composable
fun AirQualityIndexScreen() {




        Scaffold(
            bottomBar = {
                AppBottomBar()
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(it)
                    .background(Color(0xFF96D9F3))
            ) {
                items(1){
                    // Main AQI display with background scenery
                    AQIDisplay()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid of observations
                    ObservationsGrid()
                }
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
fun AQIDisplay() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {

        // This would be your actual background scenery image
        // Replace R.drawable.landscape_background with your actual drawable
        Image(
            painter = painterResource(id = R.drawable.good_weather),
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
                text = "32",
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
fun ObservationsGrid() {
    Column(
        modifier = Modifier.fillMaxWidth()
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
            ObservationCard("CO", "12.0 ppm", Modifier.weight(1f))
            ObservationCard("CO2", "12.0 ppm", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ObservationCard("NH3", "12.0 ppm", Modifier.weight(1f))
            ObservationCard("NOx", "12.0 ppm", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Third row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ObservationCard("LPG", "12.0 ppm", Modifier.weight(1f))
            ObservationCard("Methane", "12.0 ppm", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fourth row - Single card centered
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ObservationCard("Hydrogen", "12.0 ppm", Modifier.width(180.dp))
        }
    }
}

@Composable
fun ObservationCard(title: String, value: String, modifier: Modifier = Modifier) {
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
                // Gauge meter placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(50))
                ) {
                    GasPpmGauge(
                        currentValue = 150f
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
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewAirQualityIndexScreen() {
    AirQualityIndexScreen()
}