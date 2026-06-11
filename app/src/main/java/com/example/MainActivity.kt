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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.draw.scale
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

    var activeSubTab by remember { mutableStateOf(0) } // 0 = Diagnostic Shell, 1 = Sentinel Chat

    Dialog(onDismissRequest = { viewModel.setSentinelOpen(false) }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp),
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

                // Sub-tabs row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeSubTab == 0) Color(0xFFE53935).copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (activeSubTab == 0) Color(0xFFE53935) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clickable { activeSubTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (activeSubTab == 0) Color(0xFFE53935) else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Diagnostics",
                                fontSize = 11.sp,
                                color = if (activeSubTab == 0) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeSubTab == 1) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (activeSubTab == 1) Color(0xFF81C784) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clickable { activeSubTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Chat,
                                contentDescription = null,
                                tint = if (activeSubTab == 1) Color(0xFF81C784) else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Sentinel Chat",
                                fontSize = 11.sp,
                                color = if (activeSubTab == 1) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (activeSubTab == 0) {
                    // --- SUB-TAB 0: DIAGNOSTICS & HARD SELF-HEALING ---
                    val isAutoModel by viewModel.isSentinelAutoModel.collectAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sentinel AI Brain Model Source:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isAutoModel) "Auto-Route [ON]" else "Manual Mode",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isAutoModel) Color(0xFF00FFCC) else Color.Gray
                            )
                            Switch(
                                checked = isAutoModel,
                                onCheckedChange = { viewModel.setSentinelAutoModel(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF00FFCC),
                                    checkedTrackColor = Color(0xFF00FFCC).copy(alpha = 0.3f),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.scale(0.7f).height(24.dp)
                            )
                        }
                    }

                    if (isAutoModel) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161F).copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFCC),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Optima Agent routed brain spec: $selectedModel",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // Model Selector Array
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
                    }

                    Spacer(modifier = Modifier.height(6.dp))
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
                        "App is not responding" to "05-31 16:12:20.123 679 E/ActivityManager: ANR in com.aistudio.mobileoptimiser.qywxkz (com.example.MainActivity) - Reason: Input dispatching timed out (Waiting to send key or touch event) - App is not responding.",
                        "KSP AWT NPE" to "Exception in thread \"AWT-EventQueue-0\" java.lang.NullPointerException: Cannot invoke \"ksp.com.intellij.openapi.application.Application.getService(java.lang.Class)\" because the return value of \"ksp.com.intellij.openapi.application.ApplicationManager.getApplication()\" is null",
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

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = currentTargetError,
                        onValueChange = { viewModel.setSentinelSelectedError(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
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

                            Spacer(modifier = Modifier.height(4.dp))

                            // Progress indicator
                            if (status == "SCANNING" || status == "HEALING") {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp),
                                    color = if (status == "SCANNING") Color.Yellow else Color.Cyan,
                                    trackColor = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
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
                            detections.take(2).forEach { fault ->
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
                                .height(100.dp),
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
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
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
                } else {
                    // --- SUB-TAB 1: HEALTH INTERACTIVE CHAT UI ---
                    val chatMessages by viewModel.sentinelChatMessages.collectAsState()
                    val chatInput by viewModel.sentinelChatInput.collectAsState()
                    val pendingPermission by viewModel.sentinelPendingPermissionAction.collectAsState()

                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF07070A)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chatMessages) { msg ->
                                    val isAssistant = msg.sender == "assistant"
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = if (isAssistant) Alignment.Start else Alignment.End
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 10.dp,
                                                        topEnd = 10.dp,
                                                        bottomStart = if (isAssistant) 0.dp else 10.dp,
                                                        bottomEnd = if (isAssistant) 10.dp else 0.dp
                                                    )
                                                )
                                                .background(
                                                    if (isAssistant) Color(0xFF1E2130)
                                                    else Color(0xFF2E7D32).copy(alpha = 0.4f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isAssistant) Color.White.copy(alpha = 0.1f)
                                                    else Color(0xFF2E7D32).copy(alpha = 0.8f),
                                                    RoundedCornerShape(
                                                        topStart = 10.dp,
                                                        topEnd = 10.dp,
                                                        bottomStart = if (isAssistant) 0.dp else 10.dp,
                                                        bottomEnd = if (isAssistant) 10.dp else 0.dp
                                                    )
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = msg.text,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp,
                                                fontFamily = if (msg.text.contains("REPORT") || msg.text.contains("====")) FontFamily.Monospace else FontFamily.SansSerif
                                            )
                                        }
                                        Text(
                                            text = if (isAssistant) "Sentinel Assistant" else "Administrator",
                                            fontSize = 8.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Pending Diagnostic Authorization Alert Banner!
                        if (pendingPermission != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.executePendingDiagnosis() },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE53935).copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Run Protocol",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("EXECUTE DIAGNOSIS PROTOCOL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Tap to authorize Sentinel self-healing pipeline", fontSize = 8.sp, color = Color.LightGray)
                                    }
                                    Button(
                                        onClick = { viewModel.executePendingDiagnosis() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Authorize", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Chat Input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { viewModel.setSentinelChatInput(it) },
                                modifier = Modifier.weight(1f).height(46.dp),
                                textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                                placeholder = { Text("Ask about health, type 'Report'...", fontSize = 10.sp, color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2E7D32),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    focusedContainerColor = Color(0xFF16161F),
                                    unfocusedContainerColor = Color(0xFF16161F)
                                ),
                                singleLine = true
                            )

                            IconButton(
                                onClick = { 
                                    if (chatInput.isNotBlank()) {
                                        viewModel.sendSentinelChatMessage(chatInput)
                                    }
                                },
                                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2E7D32))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send Message",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
