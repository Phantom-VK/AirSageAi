package com.vikram.airsageai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vikram.airsageai.MyApp
import com.vikram.airsageai.ui.components.CustomDropdown
import com.vikram.airsageai.utils.AQINotificationWorker
import com.vikram.airsageai.utils.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController, paddingValues: PaddingValues, themeColor: Color) {
    val scrollState = rememberScrollState()
    var isDarkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("English") }

    val context = LocalContext.current
    val preferencesManager by lazy {
        PreferencesManager(context)
    }
    val app = context.applicationContext as MyApp

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        preferencesManager.getNotificationEnabled().collect{
            notificationsEnabled = it
        }
    }



    Column(
        modifier = Modifier
            .background(color = themeColor)
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)

    ) {
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        SettingCard(title = "Language", themeColor) {
            CustomDropdown(
                placeHolder = selectedLanguage,
                options = listOf("English", "Hindi", "Marathi", "Telugu"),
                onOptionSelected = { selectedLanguage = it }
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Notification Toggle
        SettingCard(title = "Notifications", themeColor) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Notifications", fontSize = 16.sp)
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        coroutineScope.launch {
                            preferencesManager.setNotificationEnabled(it)
                        }
                        if (it) {
                            app.scheduleAQINotificationWorker(context)
                        } else {
                            app.cancelAQINotificationWorker(context)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Theme Toggle
        SettingCard(title = "Appearance", themeColor) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", fontSize = 16.sp)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Account Section
        SettingCard(title = "Account", themeColor) {
            Button(
                onClick = {
                    // Add logout logic here
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Version
        Text(
            text = "App Version 1.0.0",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}



@Composable
fun SettingCard(title: String,color: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewSettingsScreen(){
    SettingsScreen(rememberNavController(), PaddingValues(),Color(0xFFFFC2A5) )
}
