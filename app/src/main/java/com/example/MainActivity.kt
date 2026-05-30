package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.OptimiserViewModel

enum class ScreenTab {
    DASHBOARD,
    AI_CHAT,
    LOGS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: OptimiserViewModel = viewModel()
            val isDark by viewModel.isDarkTheme.collectAsState()
            val isHighContrast by viewModel.isHighContrast.collectAsState()

            MyApplicationTheme(darkTheme = isDark, dynamicColor = !isHighContrast) {
                var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }

                // Define accessibility-friendly palettes based on custom state
                val activeBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.background
                val navBarBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface
                val navBarBorderColor = if (isHighContrast) Color.White else Color.Transparent
                val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onBackground

                Scaffold(
                    modifier = Modifier.fillMaxSize().background(activeBgColor),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .border(1.dp, navBarBorderColor)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar"),
                            containerColor = navBarBgColor
                        ) {
                            val items = listOf(
                                Triple(ScreenTab.DASHBOARD, Icons.Filled.Speed, Icons.Outlined.Speed),
                                Triple(ScreenTab.AI_CHAT, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
                                Triple(ScreenTab.LOGS, Icons.Filled.History, Icons.Outlined.History),
                                Triple(ScreenTab.SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings)
                            )

                            items.forEach { (tab, filledIcon, outlinedIcon) ->
                                val isSelected = currentTab == tab
                                val label = when (tab) {
                                    ScreenTab.DASHBOARD -> "Optimise"
                                    ScreenTab.AI_CHAT -> "AI Agent"
                                    ScreenTab.LOGS -> "History"
                                    ScreenTab.SETTINGS -> "Settings"
                                }

                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) filledIcon else outlinedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        unselectedTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        indicatorColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentTab,
                        animationSpec = tween(durationMillis = 150),
                        modifier = Modifier.padding(innerPadding).fillMaxSize()
                    ) { tab ->
                        when (tab) {
                            ScreenTab.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            ScreenTab.AI_CHAT -> ChatScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            ScreenTab.LOGS -> LogScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            ScreenTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
