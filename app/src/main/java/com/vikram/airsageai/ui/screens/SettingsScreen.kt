package com.vikram.airsageai.ui.screens

import android.R.attr.fontWeight
import android.app.Activity
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vikram.airsageai.MyApp
import com.vikram.airsageai.ui.components.CustomDropdown
import com.vikram.airsageai.utils.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    themeColor: Color
) {
    val scrollState = rememberScrollState()
    var notificationsEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val preferencesManager by lazy { PreferencesManager(context) }
    val app = context.applicationContext as MyApp
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(true) {
        preferencesManager.getNotificationEnabled().collect {
            notificationsEnabled = it
        }
    }

    Column(
        modifier = Modifier
            .background(themeColor.copy(alpha = 0.1f))
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(20.dp)
        )

        Spacer(modifier = Modifier.height(7.dp))

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

        SettingCard(title = "App", themeColor) {
            Button(
                onClick = {
                    (context as? Activity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Exit App", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "App Version 1.0.0",
            fontSize = 12.sp,
            color = Color.Gray

        )
    }
}




@Composable
fun SettingCard(title: String, themeColor: Color, content: @Composable ColumnScope.() -> Unit) {
    val cardColor = themeColor.copy(alpha = 0.2f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
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
