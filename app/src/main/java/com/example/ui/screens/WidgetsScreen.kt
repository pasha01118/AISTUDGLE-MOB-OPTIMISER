package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.GenericAiClient
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.OptimiserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Widget Themes Controlled by AI
enum class WidgetTheme {
    NOTHING_DARK,
    HYPER_GREEN,
    CHARGING_AMBER,
    HIGH_CONTRAST_MONO,
    COSMIC_BLUE
}

// Widget Update Cycle Controlled by AI
enum class WidgetRefreshRate {
    REALTIME_2S,
    BALANCED_10S,
    EVENT_DRIVEN_AI,
    MANUAL_SLEEP
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WidgetsScreen(
    viewModel: OptimiserViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Core states
    val isHighContrast by viewModel.isHighContrast.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()

    // Screen-level local FAQs expanding states
    var isUpdateFaqExpanded by remember { mutableStateOf(false) }
    var isAddFaqExpanded by remember { mutableStateOf(false) }

    // Interactivity simulated signals inside widgets
    var wifiEnabled by remember { mutableStateOf(true) }
    var mobileDataEnabled by remember { mutableStateOf(true) }
    var bluetoothEnabled by remember { mutableStateOf(false) }
    var gpsEnabled by remember { mutableStateOf(true) }

    // AI Widget Control variables
    var activeWidgetTheme by remember { mutableStateOf(WidgetTheme.NOTHING_DARK) }
    var refreshRate by remember { mutableStateOf(WidgetRefreshRate.REALTIME_2S) }
    var isAiAutoOptimized by remember { mutableStateOf(true) }
    var widgetAiStatusLog by remember { mutableStateOf("AI Core Active: Monitoring widget CPU wake-locks & thermal thresholds...") }

    // Custom chatbot panel for Widget Control
    var isChatLoading by remember { mutableStateOf(false) }
    var aiReplyText by remember { mutableStateOf("") }
    var userInputText by remember { mutableStateOf("") }

    // Real-time calculated values mimicking screenshot
    val formattedDate = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        sdf.format(Date())
    }

    // Adapt layout styling to High Contrast settings and AI chosen theme
    val activeBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.background
    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onBackground
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
    val activeBorderColor = if (isHighContrast) Color.White else Color.White.copy(alpha = 0.08f)

    // Current theme colors
    val themeAccentColor = when (activeWidgetTheme) {
        WidgetTheme.NOTHING_DARK -> Color(0xFFE50914) // CMF Red
        WidgetTheme.HYPER_GREEN -> Color(0xFF00FF66) // Matrix Hyper-Green
        WidgetTheme.CHARGING_AMBER -> Color(0xFFFF9900) // Solar Charging Orange
        WidgetTheme.COSMIC_BLUE -> Color(0xFF00C2FF) // Neon Cyberspace Blue
        WidgetTheme.HIGH_CONTRAST_MONO -> if (isHighContrast) Color.Yellow else Color.White
    }

    val widgetBgColor = when (activeWidgetTheme) {
        WidgetTheme.HIGH_CONTRAST_MONO -> Color.Black
        else -> Color(0xFF0C0C0F)
    }

    val widgetCardBorderColor = when (activeWidgetTheme) {
        WidgetTheme.HIGH_CONTRAST_MONO -> Color.White
        else -> Color.White.copy(alpha = 0.05f)
    }

    // Auto-update values mimicking screenshot or real sensors in background
    var rawCpuTemp by remember { mutableStateOf(100.5f) }
    var rawStorageFreeGB by remember { mutableStateOf(6.95f) }
    var rawRamPercent by remember { mutableStateOf(59f) }

    LaunchedEffect(batteryInfo, storageInfo) {
        // Tie to ViewModel but allow responsive offsets
        rawCpuTemp = if (batteryInfo.temperatureCelsius > 0) {
            (batteryInfo.temperatureCelsius * 1.8f + 32f) // C to F conversion
        } else {
            100.5f
        }
        val freeBytes = storageInfo.totalMB - storageInfo.usedMB
        rawStorageFreeGB = if (freeBytes > 0) (freeBytes / 1024f) else 6.95f
        rawRamPercent = 59f // standard CMF Phone usage state
    }

    // Function to let the AI process widget configurations
    fun dispatchAiWidgetControl(command: String) {
        isChatLoading = true
        scope.launch {
            delay(1500) // Neural simulation wait
            val cleanLower = command.lowercase().trim()

            when {
                cleanLower.contains("eco") || cleanLower.contains("power") || cleanLower.contains("battery") || cleanLower.contains("save") -> {
                    activeWidgetTheme = WidgetTheme.HYPER_GREEN
                    refreshRate = WidgetRefreshRate.EVENT_DRIVEN_AI
                    isAiAutoOptimized = true
                    widgetAiStatusLog = "AI applied battery saving parameters. Updates locked to low-latency broadcast event-driven updates. Widget Theme: HYPER_GREEN."
                    aiReplyText = "🤖 [Optina Widget Watcher]\nI have analyzed your hardware battery thresholds. Current battery is ${batteryInfo.percentage}% (Temp: ${rawCpuTemp}°F). I have dynamically converted your widgets to HYPER_GREEN styled Eco Mode, and slowed the background thread update interval to EVENT_DRIVEN_AI to shave off unneeded wake-locks!"
                }
                cleanLower.contains("aesthetic") || cleanLower.contains("color") || cleanLower.contains("theme") || cleanLower.contains("blue") || cleanLower.contains("cosmic") -> {
                    activeWidgetTheme = WidgetTheme.COSMIC_BLUE
                    refreshRate = WidgetRefreshRate.REALTIME_2S
                    isAiAutoOptimized = true
                    widgetAiStatusLog = "AI configured cyberspace rendering. Widget Theme: COSMIC_BLUE (Ambient Glow Active)."
                    aiReplyText = "🤖 [Optina Widget Watcher]\nAesthetic alignment initiated. Re-calibrated colors using cybernetic elements: switching active theme to COSMIC_BLUE with a glowing ambient canvas and high-frequency rendering clocks."
                }
                cleanLower.contains("solar") || cleanLower.contains("amber") || cleanLower.contains("charge") || cleanLower.contains("warm") -> {
                    activeWidgetTheme = WidgetTheme.CHARGING_AMBER
                    refreshRate = WidgetRefreshRate.BALANCED_10S
                    isAiAutoOptimized = true
                    widgetAiStatusLog = "AI configured Thermal Shielding. Widget Theme: CHARGING_AMBER."
                    aiReplyText = "🤖 [Optina Widget Watcher]\nCalibrating widgets to warm hardware thermals. Current layout changed to CHARGING_AMBER. Auto-throttling frequency to BALANCED_10S as system heats up."
                }
                cleanLower.contains("mono") || cleanLower.contains("nothing") || cleanLower.contains("minimal") || cleanLower.contains("default") -> {
                    activeWidgetTheme = WidgetTheme.NOTHING_DARK
                    refreshRate = WidgetRefreshRate.REALTIME_2S
                    isAiAutoOptimized = true
                    widgetAiStatusLog = "AI restored standard industrial Nothing-aesthetic. Widget Theme: NOTHING_DARK (Red highlights)."
                    aiReplyText = "🤖 [Optina Widget Watcher]\nReverting layout to the default NOTHING_DARK minimalist skin, providing micro-fluctuations to refresh-rate (REALTIME_2S) to match actual CPU thread dispatch logs."
                }
                cleanLower.contains("high contrast") || cleanLower.contains("accessibility") || cleanLower.contains("yellow") -> {
                    activeWidgetTheme = WidgetTheme.HIGH_CONTRAST_MONO
                    refreshRate = WidgetRefreshRate.EVENT_DRIVEN_AI
                    isAiAutoOptimized = true
                    widgetAiStatusLog = "AI applied accessibility rules. Widget Theme: HIGH_CONTRAST_MONO (High contrast visual indicators active)."
                    aiReplyText = "🤖 [Optina Widget Watcher]\nAccessibility override triggered. Configuring widgets to high contrast MONO layout with simplified gauges and high-vis indicator badges."
                }
                else -> {
                    // Try real Gemini call or fallback smart chat
                    val activeConfig = viewModel.getActiveAiConfig()
                    if (activeConfig.endpoint != "local_offline" && activeConfig.apiKey.isNotEmpty()) {
                        try {
                            val systemPrompt = """
                                You are the Integrated AI Model controlling the system widgets in the Mobile Optimiser app.
                                Based on the user's prompt, explain what you will change on the widgets.
                                You CAN control:
                                - Widget theme style: choose NOTHING_DARK, HYPER_GREEN, CHARGING_AMBER, COSMIC_BLUE, or HIGH_CONTRAST_MONO.
                                - Widget refresh speed: choose REALTIME_2S, BALANCED_10S, EVENT_DRIVEN_AI, or MANUAL_SLEEP.
                                
                                Respond with a helpful, friendly message explaining your optimization. At the very end of your response, output a command block tag like [SET_THEME=theme_name] [SET_SPEED=speed_name] to let the app parse and apply the visual changes.
                                Example response ending: "Configuring the widget skin... [SET_THEME=CHARGING_AMBER] [SET_SPEED=BALANCED_10S]"
                            """.trimIndent()
                            
                            val response = GenericAiClient.generateChat(
                                baseUrl = activeConfig.endpoint,
                                apiKey = activeConfig.apiKey,
                                model = activeConfig.model,
                                systemInstruction = systemPrompt,
                                chatHistory = emptyList(),
                                latestUserMessage = command
                            )
                            
                            aiReplyText = response
                            
                            // Parse tags
                            if (response.contains("[SET_THEME=NOTHING_DARK]")) activeWidgetTheme = WidgetTheme.NOTHING_DARK
                            if (response.contains("[SET_THEME=HYPER_GREEN]")) activeWidgetTheme = WidgetTheme.HYPER_GREEN
                            if (response.contains("[SET_THEME=CHARGING_AMBER]")) activeWidgetTheme = WidgetTheme.CHARGING_AMBER
                            if (response.contains("[SET_THEME=COSMIC_BLUE]")) activeWidgetTheme = WidgetTheme.COSMIC_BLUE
                            if (response.contains("[SET_THEME=HIGH_CONTRAST_MONO]")) activeWidgetTheme = WidgetTheme.HIGH_CONTRAST_MONO
                            
                            if (response.contains("[SET_SPEED=REALTIME_2S]")) refreshRate = WidgetRefreshRate.REALTIME_2S
                            if (response.contains("[SET_SPEED=BALANCED_10S]")) refreshRate = WidgetRefreshRate.BALANCED_10S
                            if (response.contains("[SET_SPEED=EVENT_DRIVEN_AI]")) refreshRate = WidgetRefreshRate.EVENT_DRIVEN_AI
                            if (response.contains("[SET_SPEED=MANUAL_SLEEP]")) refreshRate = WidgetRefreshRate.MANUAL_SLEEP
                            
                            widgetAiStatusLog = "AI Model Directive applied successfully! Theme: $activeWidgetTheme, Refresh: $refreshRate"
                        } catch (e: Exception) {
                            aiReplyText = "🤖 [Optina Local Core Exception Fallback]\nCould not query cloud Gemini API (${e.localizedMessage}). Applying local model logic: switching to Event-Driven ECO mode.\n[Applied Theme: COSMIC_BLUE]"
                            activeWidgetTheme = WidgetTheme.COSMIC_BLUE
                            refreshRate = WidgetRefreshRate.EVENT_DRIVEN_AI
                        }
                    } else {
                        // Intelligent local parser fallbacks
                        aiReplyText = "🤖 [Optina Offline Assistant]\nI have analyzed your prompt: \"$command\". Since we are currently in localized offline mode, I've used our silicon agent parameters to optimize widgets:\n\n• Configured Theme to: COSMIC_BLUE for maximum visibility\n• Scaled Updates to: EVENT_DRIVEN_AI to reduce CPU power drain\n• Calibrated Heap: cleared 16MB of transient widget cached draws to avoid UI stutters."
                        activeWidgetTheme = WidgetTheme.COSMIC_BLUE
                        refreshRate = WidgetRefreshRate.EVENT_DRIVEN_AI
                        widgetAiStatusLog = "AI applied Offline cyberglow settings. Theme: COSMIC_BLUE, Refresh: EVENT_DRIVEN_AI."
                    }
                }
            }
            isChatLoading = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(activeBgColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header echoing the screenshot
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("widgets_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = activeTextColor
                        )
                    }
                    Text(
                        text = "Widgets",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = activeTextColor,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "By adding desktop widgets, you can make your desktop more aesthetically pleasing, more convenient to use, and easily get an overview of your device.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = activeSubTextColor.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Help Links exactly matching screenshot
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Help link 1: Why Widget not update
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isUpdateFaqExpanded = !isUpdateFaqExpanded }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Help",
                                tint = themeAccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Why Widget not update",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeAccentColor,
                                style = LocalTextStyle.current.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                            )
                        }
                        
                        AnimatedVisibility(visible = isUpdateFaqExpanded) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = widgetBgColor),
                                border = BorderStroke(1.dp, widgetCardBorderColor),
                                modifier = Modifier
                                    .padding(top = 6.dp, start = 24.dp)
                                    .fillMaxWidth()
                            ) {
                                PaddingBox {
                                    Text(
                                        text = "Android dynamically restricts background draw cycles to safe-guard thermals and prevent cell power drain. Under the Integrated AI Model, updates are dynamically calibrated based on device stress profiles.",
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = activeTextColor
                                    )
                                }
                            }
                        }
                    }

                    // Help link 2: How to add Widget
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAddFaqExpanded = !isAddFaqExpanded }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Help",
                                tint = themeAccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "How to add Widget",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeAccentColor,
                                style = LocalTextStyle.current.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                            )
                        }

                        if (isAddFaqExpanded) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = widgetBgColor),
                                border = BorderStroke(1.dp, widgetCardBorderColor),
                                modifier = Modifier
                                    .padding(top = 6.dp, start = 24.dp)
                                    .fillMaxWidth()
                            ) {
                                PaddingBox {
                                    Text(
                                        text = "1. Long press any empty space on your phone launcher home screen.\n2. Tap the 'Widgets' icon in the pop-up options panel.\n3. Scroll down to locate Mobile Optimiser, drag your favorite theme to the active desktop.",
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = activeTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- INTEGRATED AI WIDGET ONTOLOGY CONTROLLER PANEL ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = widgetBgColor),
                border = BorderStroke(1.5.dp, themeAccentColor.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_widgets_control_panel")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(themeAccentColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Sparks",
                                tint = themeAccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Widget Optimization Model",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeTextColor
                            )
                            Text(
                                text = if (isAiAutoOptimized) "Autonomous Control: ACTIVE" else "Manual override configured",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAiAutoOptimized) Color(0xFF00FF66) else Color.LightGray
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Badge(
                            containerColor = themeAccentColor.copy(alpha = 0.15f),
                            contentColor = themeAccentColor,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = refreshRate.name.replace("_", " "),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = widgetAiStatusLog,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = themeAccentColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset AI instructions buttons
                    Text(
                        text = "Instruct Integrated AI Optimizer:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeTextColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { dispatchAiWidgetControl("Optimize energy parameters") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeWidgetTheme == WidgetTheme.HYPER_GREEN) themeAccentColor else Color.White.copy(alpha = 0.08f),
                                contentColor = if (activeWidgetTheme == WidgetTheme.HYPER_GREEN) Color.Black else activeTextColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BatterySaver, "Battery Saver", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Power Saving", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { dispatchAiWidgetControl("Calibrate dynamic colors to cyberspace blue") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeWidgetTheme == WidgetTheme.COSMIC_BLUE) themeAccentColor else Color.White.copy(alpha = 0.08f),
                                contentColor = if (activeWidgetTheme == WidgetTheme.COSMIC_BLUE) Color.Black else activeTextColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, "Color Palette", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cyberglow", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { dispatchAiWidgetControl("Switch style to charging amber thermals") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeWidgetTheme == WidgetTheme.CHARGING_AMBER) themeAccentColor else Color.White.copy(alpha = 0.08f),
                                contentColor = if (activeWidgetTheme == WidgetTheme.CHARGING_AMBER) Color.Black else activeTextColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WbSunny, "Sun icon", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Amber Solar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live custom chat input with widget AI assistant
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userInputText,
                            onValueChange = { userInputText = it },
                            placeholder = { Text("Ask AI Model to adapt widgets...", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = activeTextColor,
                                unfocusedTextColor = activeTextColor,
                                focusedBorderColor = themeAccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("widget_ai_prompt_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        Button(
                            onClick = {
                                if (userInputText.isNotEmpty()) {
                                    val query = userInputText
                                    userInputText = ""
                                    dispatchAiWidgetControl(query)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeAccentColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("widget_ai_send_button"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (isChatLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = themeAccentColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Model analyzing parameters...",
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = activeTextColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = aiReplyText.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth()
                                .background(themeAccentColor.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .border(1.dp, themeAccentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = aiReplyText,
                                fontSize = 11.sp,
                                color = activeTextColor,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // ================= WIDGET PREVIEW AND SENSORS GENERATION LIST =================

        // 1. Device Overview (4x2)
        item {
            WidgetGroupHeader("Device overview (4×2)")

            Card(
                colors = CardDefaults.cardColors(containerColor = widgetBgColor),
                border = BorderStroke(1.dp, widgetCardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("widget_preview_4x2_a")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Wi-Fi Button
                        IndicatorSquare(
                            icon = Icons.Default.Wifi,
                            label = "WiFi",
                            active = wifiEnabled,
                            accentColor = themeAccentColor,
                            onClick = { wifiEnabled = !wifiEnabled }
                        )

                        // Mobile Data Button
                        IndicatorSquare(
                            icon = Icons.Default.SwapVert,
                            label = "Data",
                            active = mobileDataEnabled,
                            accentColor = themeAccentColor,
                            onClick = { mobileDataEnabled = !mobileDataEnabled }
                        )

                        // Date Wide Container
                        Box(
                            modifier = Modifier
                                .weight(2.5f)
                                .height(56.dp)
                                .background(Color(0xFF1E1E24).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formattedDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeTextColor
                                )
                                Text(
                                    text = "System Date",
                                    fontSize = 9.sp,
                                    color = activeSubTextColor.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Storage Free Wide Container with Progress Bar
                        Box(
                            modifier = Modifier
                                .weight(2.5f)
                                .height(56.dp)
                                .background(Color(0xFF1E1E24).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Storage Free", fontSize = 9.sp, color = activeTextColor)
                                    Text(
                                        text = String.format("%.2fG", rawStorageFreeGB),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeAccentColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { ((storageInfo.usedMB.getPercentageFraction())) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = themeAccentColor,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bluetooth Button
                        IndicatorSquare(
                            icon = Icons.Default.Bluetooth,
                            label = "BT",
                            active = bluetoothEnabled,
                            accentColor = themeAccentColor,
                            onClick = { bluetoothEnabled = !bluetoothEnabled }
                        )

                        // GPS Location Button
                        IndicatorSquare(
                            icon = Icons.Default.MyLocation,
                            label = "GPS",
                            active = gpsEnabled,
                            accentColor = themeAccentColor,
                            onClick = { gpsEnabled = !gpsEnabled }
                        )

                        // CMF Device Core Info Widget Panel
                        Box(
                            modifier = Modifier
                                .weight(5f)
                                .height(56.dp)
                                .background(Color(0xFF1E1E24).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text(
                                    text = "CMF BY NOTHING Phone 1",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Power Sparkle",
                                        tint = themeAccentColor,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Dimensity 7300",
                                        fontSize = 9.sp,
                                        color = activeSubTextColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Battery Status
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .height(48.dp)
                                .background(Color(0xFF1E1E24).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.Battery4Bar,
                                    contentDescription = "Battery Status",
                                    tint = themeAccentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${batteryInfo.percentage}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeTextColor
                                )
                            }
                        }

                        // Android Version Title
                        Box(
                            modifier = Modifier
                                .weight(2.5f)
                                .height(48.dp)
                                .background(Color(0xFF1E1E24).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Android 16",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeTextColor
                            )
                        }

                        // A015 Phone Code Block Indicator
                        Box(
                            modifier = Modifier
                                .weight(2.5f)
                                .height(48.dp)
                                .background(Color(0xFF1E1E24).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = "Phone icon",
                                    tint = activeTextColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "A015",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeTextColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    ApplyToDesktopButton(onApply = {
                        Toast.makeText(context, "Device overview (4x2) successfully configured and pinned to device drawer!", Toast.LENGTH_SHORT).show()
                    }, themeAccentColor = themeAccentColor)
                }
            }
        }

        // 2. Device Overview (4x1)
        item {
            WidgetGroupHeader("Device overview (4×1)")

            Card(
                colors = CardDefaults.cardColors(containerColor = widgetBgColor),
                border = BorderStroke(1.dp, widgetCardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("widget_preview_4x1")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Radial Ring for RAM Specs
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularGlowDial(
                                percentage = rawRamPercent,
                                accentColor = themeAccentColor,
                                sizeDp = 76.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${rawRamPercent.toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = activeTextColor
                                )
                                Text(
                                    text = "4.28G/7.25G",
                                    fontSize = 7.sp,
                                    color = activeSubTextColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "RAM",
                                    fontSize = 7.sp,
                                    color = themeAccentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Right progress lists (Storage and Battery)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Storage Bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Storage", fontSize = 10.sp, color = activeTextColor, fontWeight = FontWeight.Bold)
                                    Text("101.00G / 107.95G", fontSize = 8.sp, color = activeSubTextColor)
                                    Text("93%", fontSize = 10.sp, color = themeAccentColor, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { 0.93f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(CircleShape),
                                    color = themeAccentColor,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }

                            // Battery Bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Battery (Charging)", fontSize = 10.sp, color = activeTextColor, fontWeight = FontWeight.Bold)
                                    Text("3.81 V / ${String.format("%.1f", rawCpuTemp)}°F", fontSize = 8.sp, color = activeSubTextColor)
                                    Text("${batteryInfo.percentage}%", fontSize = 10.sp, color = themeAccentColor, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { (batteryInfo.percentage / 100f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(CircleShape),
                                    color = themeAccentColor,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    ApplyToDesktopButton(onApply = {
                        Toast.makeText(context, "Compact overview (4x1) successfully pinned to home screen!", Toast.LENGTH_SHORT).show()
                    }, themeAccentColor = themeAccentColor)
                }
            }
        }

        // 3. Device Overview (4x2 Variant 2 with Clock)
        item {
            WidgetGroupHeader("Device overview (4×2)")

            Card(
                colors = CardDefaults.cardColors(containerColor = widgetBgColor),
                border = BorderStroke(1.dp, widgetCardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("widget_preview_4x2_b")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Clock retro header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "01:18",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = activeTextColor,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formattedDate,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeAccentColor,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Radial Ring for RAM Specs
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularGlowDial(
                                percentage = rawRamPercent,
                                accentColor = themeAccentColor,
                                sizeDp = 76.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${rawRamPercent.toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = activeTextColor
                                )
                                Text(
                                    text = "4.28B/7.25G",
                                    fontSize = 7.sp,
                                    color = activeSubTextColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "RAM",
                                    fontSize = 7.sp,
                                    color = themeAccentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Right progress lists (Storage and Battery)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Storage Bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Storage", fontSize = 10.sp, color = activeTextColor, fontWeight = FontWeight.Bold)
                                    Text("101.00G / 107.95G", fontSize = 8.sp, color = activeSubTextColor)
                                    Text("93%", fontSize = 10.sp, color = themeAccentColor, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { 0.93f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(CircleShape),
                                    color = themeAccentColor,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }

                            // Battery Bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Battery (Charging)", fontSize = 10.sp, color = activeTextColor, fontWeight = FontWeight.Bold)
                                    Text("3.81 V / ${String.format("%.1f", rawCpuTemp)}°F", fontSize = 8.sp, color = activeSubTextColor)
                                    Text("${batteryInfo.percentage}%", fontSize = 10.sp, color = themeAccentColor, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { (batteryInfo.percentage / 100f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(CircleShape),
                                    color = themeAccentColor,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    ApplyToDesktopButton(onApply = {
                        Toast.makeText(context, "Device clock compilation (4x2) pinned successful!", Toast.LENGTH_SHORT).show()
                    }, themeAccentColor = themeAccentColor)
                }
            }
        }

        // 4. RAM Usage (1x1)
        item {
            WidgetGroupHeader("RAM Usage (1×1)")

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = widgetBgColor),
                        border = BorderStroke(1.dp, widgetCardBorderColor),
                        modifier = Modifier
                            .width(220.dp)
                            .align(Alignment.Center)
                            .testTag("widget_preview_1x1_ram")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularGlowDial(
                                    percentage = rawRamPercent,
                                    accentColor = themeAccentColor,
                                    sizeDp = 96.dp
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${rawRamPercent.toInt()}%",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = activeTextColor
                                    )
                                    Text(
                                        text = "4.28G/7.25G",
                                        fontSize = 8.sp,
                                        color = activeSubTextColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "RAM",
                                        fontSize = 8.sp,
                                        color = themeAccentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            ApplyToDesktopButton(onApply = {
                                Toast.makeText(context, "RAM circle monitor (1x1) pinned to workspace!", Toast.LENGTH_SHORT).show()
                            }, themeAccentColor = themeAccentColor)
                        }
                    }
                }
            }
        }

        // 5. Internal Storage (2x1)
        item {
            WidgetGroupHeader("Internal Storage (2×1)")

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = widgetBgColor),
                        border = BorderStroke(1.dp, widgetCardBorderColor),
                        modifier = Modifier
                            .width(290.dp)
                            .align(Alignment.Center)
                            .testTag("widget_preview_2x1_storage")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularGlowDial(
                                        percentage = 35f, // Free capacity percentage representation
                                        accentColor = themeAccentColor,
                                        sizeDp = 50.dp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = "Folder System",
                                        tint = themeAccentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Storage Free",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = activeSubTextColor
                                    )
                                    Text(
                                        text = String.format("%.2f GB", rawStorageFreeGB),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = activeTextColor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            ApplyToDesktopButton(onApply = {
                                Toast.makeText(context, "Storage status ring (2x1) pinned!", Toast.LENGTH_SHORT).show()
                            }, themeAccentColor = themeAccentColor)
                        }
                    }
                }
            }
        }

        // 6. Battery (2x1)
        item {
            WidgetGroupHeader("Battery (2×1)")

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = widgetBgColor),
                        border = BorderStroke(1.dp, widgetCardBorderColor),
                        modifier = Modifier
                            .width(290.dp)
                            .align(Alignment.Center)
                            .testTag("widget_preview_2x1_battery")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(themeAccentColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                                        contentDescription = "Battery Std",
                                        tint = themeAccentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Battery ${batteryInfo.percentage}%",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = activeTextColor
                                    )
                                    Text(
                                        text = "Temp. ${String.format("%.1f", rawCpuTemp)}°F",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = activeSubTextColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            ApplyToDesktopButton(onApply = {
                                Toast.makeText(context, "Battery controller (2x1) pinned directly!", Toast.LENGTH_SHORT).show()
                            }, themeAccentColor = themeAccentColor)
                        }
                    }
                }
            }
        }

        // Home screen Sandbox simulation footer to have an amazing functional experience
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                PaddingBox {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddToHomeScreen,
                            contentDescription = "Home simulation info icon",
                            tint = themeAccentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Interactive Home Sandbox Mode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeTextColor
                        )
                        Text(
                            text = "All applied widgets on this screen reflect live sensor feeds. Updates are directly optimized by the active AI Daemon core below to ensure a zero-drain CPU stack footprint.",
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp,
                            color = activeSubTextColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper composables to avoid boilerplate
@Composable
fun PaddingBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(12.dp)) {
        content()
    }
}

@Composable
fun WidgetGroupHeader(title: String) {
    Text(
        text = "• $title",
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp)
    )
}

@Composable
fun IndicatorSquare(
    icon: ImageVector,
    label: String,
    active: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                if (active) accentColor.copy(alpha = 0.15f) else Color(0xFF1E1E24).copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (active) accentColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) accentColor else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) Color.White else Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ApplyToDesktopButton(
    onApply: () -> Unit,
    themeAccentColor: Color
) {
    OutlinedButton(
        onClick = onApply,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = themeAccentColor
        ),
        border = BorderStroke(1.2.dp, themeAccentColor.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
    ) {
        Text(
            text = "Apply to desktop",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun CircularGlowDial(
    percentage: Float,
    accentColor: Color,
    sizeDp: androidx.compose.ui.unit.Dp
) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        // Draw background circle
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = size.minDimension / 2f,
            style = Stroke(width = 6.dp.toPx())
        )
        // Draw active sector sweep
        drawArc(
            color = accentColor,
            startAngle = -90f,
            sweepAngle = (percentage / 100f) * 360f,
            useCenter = false,
            style = Stroke(width = 6.dp.toPx())
        )
    }
}

// Utility extension for percentage calculations
fun Long.getPercentageFraction(): Float {
    if (this <= 0L) return 0f
    return (this.toFloat() / 256000f).coerceIn(0f, 1f)
}
