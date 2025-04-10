package com.vikram.airsageai.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun InfoScreen(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Air Quality Guidelines",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Section 1: Guidelines
        Text(
            text = "Recommended Safety Levels for Gases",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(text = "• CO (Carbon Monoxide): Above 50 PPM can be harmful.\n" +
                "• CO₂ (Carbon Dioxide): Over 1000 PPM may cause drowsiness.\n" +
                "• NH₃ (Ammonia): More than 25 PPM is dangerous.\n" +
                "• NOx (Nitrogen Oxides): Over 1 PPM may affect respiratory health.\n" +
                "• LPG: Should not exceed 1000 PPM in closed areas.\n" +
                "• Methane: Highly flammable above 5000 PPM.\n" +
                "• Hydrogen: Risk of explosion at high concentrations.")

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Mask Usage
        Text(
            text = "When Should You Wear a Mask?",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(text = "• If CO or CO₂ levels rise above safe limits.\n" +
                "• During high NH₃ or NOx concentrations (especially near industrial areas).\n" +
                "• When outdoor AQI is above 150 (Unhealthy category).\n" +
                "• Recommended: Use N95/N99 masks for PM2.5 and harmful gas protection.")

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Health Threats
        Text(
            text = "Effects of Air Pollution on Human Health",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(text = "• Short-term exposure: Headaches, eye irritation, coughing.\n" +
                "• Long-term exposure: Lung damage, heart disease, cancer risk.\n" +
                "• Children and elderly are more vulnerable.\n" +
                "• Chronic exposure to pollutants like CO and NOx may worsen asthma or bronchitis.")

        Spacer(modifier = Modifier.height(24.dp))

        // Optional back button or nav action
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back")
        }
    }
}
