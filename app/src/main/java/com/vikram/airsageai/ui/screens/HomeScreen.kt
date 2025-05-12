package com.vikram.airsageai.ui.screens

import LocationViewModel
import LocationViewModelFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.vikram.airsageai.R
import com.vikram.airsageai.data.dataclass.GasReading
import com.vikram.airsageai.data.dataclass.GasState
import com.vikram.airsageai.data.dataclass.GasThresholds
import com.vikram.airsageai.data.dataclass.gasThresholdsMap
import com.vikram.airsageai.ui.components.CircularSpeedIndicator
import com.vikram.airsageai.utils.LocationUtils
import org.apache.poi.hssf.usermodel.HeaderFooter.fontSize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    latestReading: GasReading?,
    overallAQI: Int? = null,
    themeColor: Color
) {
    // Create a dummy GasReading if latestReading.value is null to access its functions
    val gasReading = latestReading ?: GasReading()

    // Get AQI category and color based on overallAQI value
    val aqiCategory = gasReading.getAQICategory(overallAQI ?: 0)
    val aqiColor = try {
        Color(gasReading.getAQIColor(overallAQI ?: 0).toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    // Get health implications
    val healthImplications = gasReading.getHealthImplications(overallAQI ?: 0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(themeColor)
    ) {
        items(1) {
            AQIDisplay(overallAQI, aqiCategory, aqiColor)
            ObservationsGrid(latestReading)
            Spacer(modifier = Modifier.height(16.dp))
            HealthInfoCard(healthImplications, aqiColor)
            Spacer(modifier = Modifier.height(16.dp))
//            PollutantLevelsSection(latestReading)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TopAppBar(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val locationUtils = remember { LocationUtils(context) }
    val viewModelFactory = remember { LocationViewModelFactory(locationUtils, context) }
    val locationVM: LocationViewModel = viewModel(
        key = "LocationVM",
        factory = viewModelFactory
    )

    val locationName by locationVM.locationName.collectAsState()
    val errorMessage by locationVM.error.collectAsState()

    val currentDate by remember {
        mutableStateOf(
            SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                .format(Date())
        )
    }

    // Fetch location when the component is first composed


    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .size(30.dp)
                .clickable { onBackClick() }
        )

        // Location Display
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = locationName,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Current location"
            )
        }

        // Date display
        Text(
            text = currentDate,
            fontSize = 18.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun AQIDisplay(aqi: Int?, aqiCategory: String, aqiColor: Color) {
    var image: Painter= painterResource(id = R.drawable.good_weather)


    image = when (aqi) {
        in 0..50 -> painterResource(id = R.drawable.good_weather)
        in 51..100 -> painterResource(id = R.drawable.bad_weather)
        else -> painterResource(id = R.drawable.worst_weather)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {
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
                text = if(aqi != 0) aqi.toString() else "Fetching\n(Will take time on first opening)",
                fontSize = if(aqi != 0) 120.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )

            // AQI Category with background color based on AQI level
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = aqiColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = aqiCategory,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

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
fun HealthInfoCard(healthImplications: String, aqiColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Health Information",
                    tint = aqiColor,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Health Implications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                 color = Color.LightGray
            )

            Text(
                text = healthImplications,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
    }
}

//@Composable
//fun PollutantLevelsSection(gasReading: State<GasReading?>) {
//    val reading = gasReading.value ?: return
//
//    // Get individual AQI values for each pollutant
//    val aqiValues = reading.toAQI()
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 4.dp
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = "Pollutant AQI Levels",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            HorizontalDivider(color = Color.LightGray)
//
//            // Display individual pollutant AQI levels
//            PollutantAqiRow("CO", aqiValues["CO"] ?: 0, reading)
//            PollutantAqiRow("Benzene", aqiValues["Benzene"] ?: 0, reading)
//            PollutantAqiRow("NH3", aqiValues["NH3"] ?: 0, reading)
//            PollutantAqiRow("Smoke", aqiValues["Smoke"] ?: 0, reading)
//            PollutantAqiRow("LPG", aqiValues["LPG"] ?: 0, reading)
//            PollutantAqiRow("Methane", aqiValues["Methane"] ?: 0, reading)
//            PollutantAqiRow("Hydrogen", aqiValues["Hydrogen"] ?: 0, reading)
//        }
//    }
//}

@Composable
fun PollutantAqiRow(pollutant: String, aqi: Int, reading: GasReading) {
    val aqiColor = try {
        Color(reading.getAQIColor(aqi).toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = pollutant,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        // AQI value with colored background
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = aqiColor.copy(alpha = 0.2f),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$aqi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = aqiColor
                )

                if (aqi > 100) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = aqiColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Category text
        Text(
            text = reading.getAQICategory(aqi),
            fontSize = 14.sp,
            color = aqiColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
fun ObservationsGrid(gasReading: GasReading?) {
    val gases = listOf(
        "CO" to (gasReading?.CO ?: 0.0).toFloat(),
        "Benzene" to (gasReading?.Benzene ?: 0.0).toFloat(),
        "NH3" to (gasReading?.NH3 ?: 0.0).toFloat(),
        "Smoke" to (gasReading?.Smoke ?: 0.0).toFloat(),
        "LPG" to (gasReading?.LPG ?: 0.0).toFloat(),
        "Methane" to (gasReading?.CH4 ?: 0.0).toFloat(),
        "Hydrogen" to (gasReading?.H2 ?: 0.0).toFloat()
    )

    val rows = gases.chunked(2) // create pairs, last one will be solo

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Current Observations",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        rows.forEachIndexed { index, row ->
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { (name, value) ->
                    val thresholds = gasThresholdsMap[name] ?: GasThresholds(1000f, 500f, 800f)
                    ObservationCard(
                        title = name,
                        gasValue = value,
                        gasState = GasState(
                            gasValue = value,
                            maxGasValue = thresholds.max,
                            warningThreshold = thresholds.warning,
                            dangerThreshold = thresholds.danger
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                    )
                }

                // If last row has only 1 item
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
fun ObservationCard(
    title: String,
    gasValue: Float,
    gasState: GasState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(50))
                ) {
                    CircularSpeedIndicator(
                        gasState = gasState,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "%.2f".format(gasValue),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

            }
        }
    }
}
