package com.vikram.airsageai.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vikram.airsageai.R
import androidx.navigation.compose.rememberNavController
import com.vikram.airsageai.ui.screens.Screen
import com.vikram.airsageai.viewmodels.ScreenViewModel


@Composable
fun AppBottomBar(surfaceColor:Color) {
    val selectedItem = remember { mutableStateOf<Screen>(Screen.Home) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = surfaceColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    screen = Screen.Home,
                    selectedItem = selectedItem,
                    iconRes = R.drawable.home,
                    label = "Home"
                )
                BottomBarItem(
                    screen = Screen.Analytics,
                    selectedItem = selectedItem,
                    iconRes = R.drawable.analytics,
                    label = "Analytics"
                )
                BottomBarItem(
                    screen = Screen.Info,
                    selectedItem = selectedItem,
                    iconRes = R.drawable.info,
                    label = "Info"
                )
                BottomBarItem(
                    screen = Screen.Settings,
                    selectedItem = selectedItem,
                    iconRes = R.drawable.settings,
                    label = "Settings"
                )
            }
        }
    }
}

@Composable
fun BottomBarItem(
    screen: Screen,
    selectedItem: MutableState<Screen>,
    @DrawableRes iconRes: Int,
    label: String
) {
    val isSelected = selectedItem.value == screen
    val iconColor = if (isSelected) Color.Black else Color.White
    val screenViewModel: ScreenViewModel = viewModel()

    Column(
        modifier = Modifier
            .clickable {
                selectedItem.value = screen
                screenViewModel.setCurrentScreen(screen)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = iconColor
        )
    }
}

//
//@Preview(showBackground = true)
//@Composable
//fun PreviewAppBar(){
//
//    AppBottomBar()
//}