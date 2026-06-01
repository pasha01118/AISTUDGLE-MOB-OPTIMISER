package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HardwareScreen
import com.example.ui.screens.LogScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.AgentCoreScreen
import com.example.ui.screens.WidgetsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.OptimiserViewModel

enum class ScreenTab {
    DASHBOARD,
    HARDWARE,
    WIDGETS,
    AI_CHAT,
    AGENT_MIND,
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
            val isSentinelOpen by viewModel.isSentinelOpen.collectAsState()
            val showSentinelFloatingButton by viewModel.showSentinelFloatingButton.collectAsState()

            MyApplicationTheme(darkTheme = isDark, dynamicColor = !isHighContrast) {
                var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }

                val activeBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.background
                val navBarBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface
                val navBarBorderColor = if (isHighContrast) Color.White else Color.Transparent

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
                                Triple(ScreenTab.HARDWARE, Icons.Filled.Info, Icons.Outlined.Info),
                                Triple(ScreenTab.WIDGETS, Icons.Filled.Home, Icons.Outlined.Home),
                                Triple(ScreenTab.AI_CHAT, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
                                Triple(ScreenTab.AGENT_MIND, Icons.Filled.Psychology, Icons.Outlined.Psychology),
                                Triple(ScreenTab.LOGS, Icons.Filled.History, Icons.Outlined.History),
                                Triple(ScreenTab.SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings)
                            )

                            items.forEach { (tab, filledIcon, outlinedIcon) ->
                                val isSelected = currentTab == tab
                                val label = when (tab) {
                                    ScreenTab.DASHBOARD -> "Optimise"
                                    ScreenTab.HARDWARE -> "Specs"
                                    ScreenTab.WIDGETS -> "Widgets"
                                    ScreenTab.AI_CHAT -> "AI Agent"
                                    ScreenTab.AGENT_MIND -> "Agent Core"
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
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        Crossfade(
                            targetState = currentTab,
                            animationSpec = tween(durationMillis = 150),
                            modifier = Modifier.fillMaxSize()
                        ) { tab ->
                            when (tab) {
                                ScreenTab.DASHBOARD -> DashboardScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                                ScreenTab.HARDWARE -> HardwareScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                                ScreenTab.WIDGETS -> WidgetsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentTab = ScreenTab.DASHBOARD },
                                    modifier = Modifier.fillMaxSize()
                                )
                                ScreenTab.AI_CHAT -> ChatScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                                ScreenTab.AGENT_MIND -> AgentCoreScreen(
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

                        // Floating Action Button to launch AI powered Sentinel Self-Healing Core anywhere
                        if (showSentinelFloatingButton && currentTab == ScreenTab.DASHBOARD) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.setSentinelOpen(true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.error,
                                        contentColor = if (isHighContrast) Color.Black else Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .testTag("sentinel_launcher_fab")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Sentinel Shield",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AI Sentinel",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isSentinelOpen) {
                    SentinelDiagnosticModal(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SentinelDiagnosticModal(viewModel: OptimiserViewModel) {
    val status by viewModel.sentinelStatus.collectAsState()
    val logs by viewModel.sentinelLogs.collectAsState()
    val detections by viewModel.sentinelDetections.collectAsState()
    val selectedModel by viewModel.sentinelSelectedModel.collectAsState()
    val externalPromptHelp by viewModel.externalPromptHelp.collectAsState()
    val isHighContrast by viewModel.isHighContrast.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val models = listOf(
        "gemini-3.5-flash",
        "gemini-3.1-pro-preview",
        "Qwen AI 1B (Local)",
        "gpt-4o-mini"
    )

    Dialog(onDismissRequest = { viewModel.setSentinelOpen(false) }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(580.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isHighContrast) Color.Black else Color(0xFF0F0F14),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = if (isHighContrast) Color.White else Color(0xFFE53935).copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Sentinel Core",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sentinel Self-Healing Core",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setSentinelOpen(false) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Modal",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Select Sentinel AI Brain Model:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )

                // Model Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    models.forEach { model ->
                        val isSelected = selectedModel == model
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) Color(0xFFE53935).copy(alpha = 0.2f)
                                    else Color(0xFF16161F)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFE53935) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.setSentinelSelectedModel(model) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = model.replace("-", " ").replace("gemini", "gem"),
                                fontSize = 9.sp,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Select Log Target to Diagnose/Fix:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val currentTargetError by viewModel.sentinelSelectedError.collectAsState()
                val preloadedErrors = listOf(
                    "channel 'db18ee0' (Latest)" to "06-01 04:46:40.379 677 E/InputDispatcher: channel 'db18ee0 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '69ef4ce'" to "06-01 03:58:20.126 679 E/InputDispatcher: channel '69ef4ce com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '1a1acd1'" to "06-01 03:57:59.211 679 E/InputDispatcher: channel '1a1acd1 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '7c54713'" to "06-01 03:57:59.211 679 E/InputDispatcher: channel '7c54713 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '38709bf'" to "05-31 20:04:03.661 679 E/InputDispatcher: channel '38709bf com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel 'd8d6539'" to "05-31 20:03:40.453 679 E/InputDispatcher: channel 'd8d6539 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel 'e50633d'" to "05-31 20:00:32.749 679 E/InputDispatcher: channel 'e50633d com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel 'c7a8388'" to "05-31 19:02:49.197 677 E/InputDispatcher: channel 'c7a8388 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel 'e86b5a9'" to "05-31 19:00:41.017 677 E/InputDispatcher: channel 'e86b5a9 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '1a448bf'" to "05-31 19:01:03.778 677 E/InputDispatcher: channel '1a448bf com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel 'fe154a7'" to "05-31 18:58:36.290 677 E/InputDispatcher: channel 'fe154a7 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '1af9d87'" to "05-31 18:58:57.599 677 E/InputDispatcher: channel '1af9d87 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel 'dc3874'" to "05-31 18:56:48.073 677 E/InputDispatcher: channel 'dc3874 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '97430f'" to "05-31 18:56:48.073 677 E/InputDispatcher: channel '97430f com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '59c64b'" to "05-31 18:53:32.838 677 E/InputDispatcher: channel '59c64b com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '7e1dcfb'" to "05-31 18:26:55.501 677 E/InputDispatcher: channel '7e1dcfb com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '670eb96'" to "05-31 16:15:54.257 679 E/InputDispatcher: channel '670eb96 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '5324fd8'" to "05-31 16:15:54.257 679 E/InputDispatcher: channel '5324fd8 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "App is not responding" to "05-31 16:12:20.123 679 E/ActivityManager: ANR in com.aistudio.mobileoptimiser.qywxkz (com.example.MainActivity) - Reason: Input dispatching timed out (Waiting to send key or touch event) - App is not responding.",
                    "channel '132ce95'" to "05-31 16:01:06.743 679 E/InputDispatcher: channel '132ce95 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "KSP AWT NPE" to "Exception in thread \"AWT-EventQueue-0\" java.lang.NullPointerException: Cannot invoke \"ksp.com.intellij.openapi.application.Application.getService(java.lang.Class)\" because the return value of \"ksp.com.intellij.openapi.application.ApplicationManager.getApplication()\" is null",
                    "channel '7f28445'" to "05-31 15:57:07.738 679 E/InputDispatcher: channel '7f28445 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel 'e1c51b'" to "05-31 15:57:07.743 679 E/InputDispatcher: channel 'e1c51b com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '60dcbbd'" to "05-31 15:57:45.857 679 E/InputDispatcher: channel '60dcbbd com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel 'a091fd4'" to "05-31 15:27:00.272 679 E/InputDispatcher: channel 'a091fd4 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!",
                    "channel '8824a02'" to "05-30 03:05:46.345 679 E/InputDispatcher: channel '8824a02 com.aistudio.mobileoptimiser' ~ Channel is unrecoverably broken!",
                    "channel '1f06110'" to "05-30 03:06:10.596 679 E/InputDispatcher: channel '1f06110 com.aistudio.mobileoptimiser' ~ Channel is unrecoverably broken!",
                    "channel 'c8785f7'" to "05-30 03:00:23.112 679 E/InputDispatcher: channel 'c8785f7 com.aistudio.mobileoptimiser' ~ Channel is unrecoverably broken!",
                    "channel '3cb6aae'" to "05-30 03:00:40.276 679 E/InputDispatcher: channel '3cb6aae com.aistudio.mobileoptimiser' ~ Channel is unrecoverably broken!",
                    "NullPointer" to "05-30 03:15:10.820 421 E/AndroidRuntime: FATAL EXCEPTION: NullPointerException at Compose.Measure"
                )
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(preloadedErrors) { itemPair ->
                        val label = itemPair.first
                        val errString = itemPair.second
                        val isTargetSelected = currentTargetError == errString
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isTargetSelected) Color(0xFFFFB300).copy(alpha = 0.2f)
                                    else Color(0xFF16161F)
                                )
                                .border(
                                    1.dp,
                                    if (isTargetSelected) Color(0xFFFFB300) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.setSentinelSelectedError(errString) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                color = if (isTargetSelected) Color.White else Color.Gray,
                                fontWeight = if (isTargetSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentTargetError,
                    onValueChange = { viewModel.setSentinelSelectedError(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("sentinel_custom_error_input"),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    label = { Text("Inspect/Paste Custom Log Trace", fontSize = 9.sp, color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE53935),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF16161F),
                        unfocusedContainerColor = Color(0xFF16161F)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Diagnostic Console Screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF07070A)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REAL-TIME DIAGNOSTIC SHIELD SHELL",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935)
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        when (status) {
                                            "SCANNING" -> Color.Yellow
                                            "HEALING" -> Color.Cyan
                                            "RESOLVED" -> Color.Green
                                            "FAILED" -> Color(0xFFE53935)
                                            else -> Color.Gray
                                        },
                                        RoundedCornerShape(3.dp)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progress indicator
                        if (status == "SCANNING" || status == "HEALING") {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = if (status == "SCANNING") Color.Yellow else Color.Cyan,
                                trackColor = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (log.contains("CRITICAL") || log.contains("Error") || log.contains("failed")) Color(0xFFFF8A80)
                                            else if (log.contains("SUCCESSFULLY") || log.contains("SYSTEM")) Color(0xFFB9F6CA)
                                            else Color.LightGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sentinel Detected Issues List
                if (detections.isNotEmpty() && status != "RESOLVED") {
                    Column {
                        Text(
                            text = "DETECTED SYSTEM FAULTS:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8A80)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        detections.forEach { fault ->
                            Row(modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp)) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = "",
                                    tint = Color(0xFFFF8A80),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = fault,
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // If FAILED -> Prompt the User for External Code Help Box
                if (status == "FAILED" && externalPromptHelp.isNotEmpty()) {
                    Text(
                        text = "SOLVER BLOCKED. COPY EXTERNAL PROMPT:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16161F)),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Yellow.copy(alpha = 0.2f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                item {
                                    Text(
                                        text = externalPromptHelp,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(externalPromptHelp))
                                    Toast.makeText(context, "Troubleshooting Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Prompt",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Buttons Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (status == "IDLE" || status == "RESOLVED" || status == "FAILED") {
                        Button(
                            onClick = { viewModel.resetSentinel() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.0f)
                                .height(40.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Diagnostic Options
                        Button(
                            onClick = { viewModel.runSentinelDiagnostic(forceFail = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(40.dp)
                                .testTag("run_sentinel_heal_success")
                        ) {
                            Text("Self-Heal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.runSentinelDiagnostic(forceFail = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(40.dp)
                                .testTag("run_sentinel_heal_fail")
                        ) {
                            Text("Sandbox Fail", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        // Progress state - disables action
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sentinel AI solver calculating heap trees...",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
