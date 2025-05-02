package com.vikram.airsageai.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.vikram.airsageai.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    paddingValues: PaddingValues,
    themeColor: Color
) {
    val scrollState = rememberScrollState()
    val url = "https://www.airnow.gov/aqi/aqi-basics/"
    val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
            .verticalScroll(scrollState)
    ) {
        // Top Header with gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(themeColor)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Air Quality Guidelines",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Stay safe and healthy with these recommendations",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content sections
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            // Section 1: Guidelines
            GuidelineSection(
                title = "Safety Levels for Gases",
                painter = R.drawable.airpurifier,
                content = {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        GasInfoItem(
                            name = "Carbon Monoxide (CO)",
                            limit = "Above 50 PPM can be harmful",
                            color = MaterialTheme.colorScheme.error
                        )
                        GasInfoItem(
                            name = "Carbon Dioxide (CO₂)",
                            limit = "Over 1000 PPM may cause drowsiness",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        GasInfoItem(
                            name = "Ammonia (NH₃)",
                            limit = "More than 25 PPM is dangerous",
                            color = MaterialTheme.colorScheme.error
                        )
                        GasInfoItem(
                            name = "Nitrogen Oxides (NOx)",
                            limit = "Over 1 PPM may affect respiratory health",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        GasInfoItem(
                            name = "LPG",
                            limit = "Should not exceed 1000 PPM in closed areas",
                            color = MaterialTheme.colorScheme.error
                        )
                        GasInfoItem(
                            name = "Methane",
                            limit = "Highly flammable above 5000 PPM",
                            color = MaterialTheme.colorScheme.error
                        )
                        GasInfoItem(
                            name = "Hydrogen",
                            limit = "Risk of explosion at high concentrations",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                color = themeColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Mask Usage
            GuidelineSection(
                title = "When Should You Wear a Mask?",
                painter = R.drawable.face_mask,
                content = {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        BulletPointItem("If CO or CO₂ levels rise above safe limits")
                        BulletPointItem("During high NH₃ or NOx concentrations (especially near industrial areas)")
                        BulletPointItem("When outdoor AQI is above 150 (Unhealthy category)")

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.recommend),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Use N95/N99 masks for PM2.5 and harmful gas protection",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                color = themeColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Health Threats
            GuidelineSection(
                title = "Effects of Air Pollution on Health",
                painter = R.drawable.health_safety,
                content = {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        HealthEffectItem(
                            title = "Short-term exposure",
                            effects = "Headaches, eye irritation, coughing"
                        )

                        HealthEffectItem(
                            title = "Long-term exposure",
                            effects = "Lung damage, heart disease, cancer risk"
                        )

                        HealthEffectItem(
                            title = "High-risk groups",
                            effects = "Children and elderly are more vulnerable"
                        )

                        HealthEffectItem(
                            title = "Chronic conditions",
                            effects = "Exposure to pollutants like CO and NOx may worsen asthma or bronchitis"
                        )
                    }
                },
                color = themeColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = {

                        if(browserIntent.resolveActivity(context.packageManager) != null){
                            context.startActivity(browserIntent)
                        }else{
                            Toast.makeText(context, "No Browser Found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonColors(
                        containerColor = themeColor,
                        contentColor = Color.White,
                        disabledContainerColor = themeColor.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Text("Learn More")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidelineSection(
    title: String,
    painter: Int,
    content: @Composable () -> Unit,
    color: Color
) {
    var expanded by remember { mutableStateOf(true) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            // Section header (always visible)
            Surface(
                onClick = { expanded = !expanded },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        painter = painterResource(painter),
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expandable content
            if (expanded) {
                content()
            }
        }
    }
}

@Composable
fun GasInfoItem(name: String, limit: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = name,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = limit,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun BulletPointItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun HealthEffectItem(title: String, effects: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = effects,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewInfoScreens(){
    InfoScreen(PaddingValues(), Color(0xFF96D9F3))
}