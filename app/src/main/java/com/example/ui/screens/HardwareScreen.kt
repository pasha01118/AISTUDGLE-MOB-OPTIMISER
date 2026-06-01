package com.example.ui.screens

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OptimiserViewModel
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.util.Locale
import kotlin.random.Random

@Composable
fun HardwareScreen(
    viewModel: OptimiserViewModel,
    modifier: Modifier = Modifier
) {
    val isHighContrast by viewModel.isHighContrast.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    
    // Sub-Categories aligned with the screenshots
    val categories = listOf("Dashboard", "Device", "System", "CPU", "Battery", "Sensors", "Apps", "Tests")
    var selectedCategory by remember { mutableStateOf("Dashboard") }

    val activeBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.background
    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onBackground
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
    val activeBorderColor = if (isHighContrast) Color.White else Color.Transparent

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(activeBgColor)
    ) {
        // DevInfo Styled Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isHighContrast) Color.Black 
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
                .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "DevInfo Launcher",
                            tint = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "DevInfo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = activeTextColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.scanSystem() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh System Specs",
                            tint = activeTextColor
                        )
                    }
                }
            }
        }

        // Horizontal Category Tabs
        ScrollableTabRow(
            categories = categories,
            selected = selectedCategory,
            onSelect = { selectedCategory = it },
            isHighContrast = isHighContrast
        )

        Divider(
            thickness = 1.dp,
            color = if (isHighContrast) Color.White else Color.White.copy(alpha = 0.1f)
        )

        // Contents Based on Categories
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedCategory) {
                "Dashboard" -> DashboardView(viewModel = viewModel, onNavigateToTests = { selectedCategory = "Tests" }, isHighContrast = isHighContrast)
                "Device" -> DeviceView(isHighContrast = isHighContrast)
                "System" -> SystemView(isHighContrast = isHighContrast)
                "CPU" -> CpuView(isHighContrast = isHighContrast)
                "Battery" -> BatteryView(isHighContrast = isHighContrast)
                "Sensors" -> SensorsView(isHighContrast = isHighContrast)
                "Apps" -> AppsDiagnosticView(isHighContrast = isHighContrast)
                "Tests" -> DiagnosticTestsView(isHighContrast = isHighContrast)
            }
        }
    }
}

@Composable
fun ScrollableTabRow(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    isHighContrast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selected == category
            val activeTabBg = if (isSelected) {
                if (isHighContrast) Color.White else MaterialTheme.colorScheme.primaryContainer
            } else {
                if (isHighContrast) Color.Black else Color.Transparent
            }
            val activeTabBorder = if (isHighContrast) {
                Color.White
            } else {
                if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)
            }
            val activeTabText = if (isSelected) {
                if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                if (isHighContrast) Color.White else Color.Gray
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(activeTabBg)
                    .border(1.dp, activeTabBorder, RoundedCornerShape(20.dp))
                    .clickable { onSelect(category) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = activeTabText
                )
            }
        }
    }
}

// ==================== 1. DASHBOARD VIEW ====================
@Composable
fun DashboardView(
    viewModel: OptimiserViewModel,
    onNavigateToTests: () -> Unit,
    isHighContrast: Boolean
) {
    val scrollState = rememberScrollState()
    
    val isCpuOverclocked by viewModel.isCpuOverclocked.collectAsState()
    
    // System simulated metrics
    var coreFreqs by remember(isCpuOverclocked) { 
        val baseList = if (isCpuOverclocked) {
            List(8) { Random.nextInt(2350, 2500) }
        } else {
            List(8) { Random.nextInt(400, 2500) }
        }
        mutableStateOf(baseList)
    }
    var ramPct by remember { mutableStateOf(58f) }
    var storagePct by remember { mutableStateOf(93f) }

    // Fluctuating stats simulating active background load
    LaunchedEffect(isCpuOverclocked) {
        while (true) {
            delay(1500)
            coreFreqs = coreFreqs.map { 
                if (isCpuOverclocked) {
                    Random.nextInt(2350, 2500)
                } else {
                    val delta = Random.nextInt(-150, 150)
                    (it + delta).coerceIn(400, 2500)
                }
            }
            ramPct = (58f + Random.nextFloat() * 3f).coerceIn(40f, 95f)
        }
    }

    val cardBg = if (isHighContrast) Color.Black else Color(0xFF0F0F14)
    val cardBorder = if (isHighContrast) Color.White else Color.White.copy(alpha = 0.1f)
    val textPrimary = Color.White
    val textAccent = if (isHighContrast) Color.Yellow else Color(0xFF00E676)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Brand Launcher Device Header Card (Matches Page 1 NOTHING A015 ANDROID 16)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "Nothing Device",
                        tint = textAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("NOTHING", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textPrimary)
                        Text("A015", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "Android Version",
                        tint = textAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("ANDROID 16", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text("Baklava", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Gauges & Storage Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, if (isHighContrast) Color.White else Color(0xFF00E676).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // RAM Gauge Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        if (isHighContrast) Color.Black else Color(0xFF00E676).copy(alpha = 0.1f), 
                                        CircleShape
                                    )
                                    .border(2.dp, textAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${ramPct.toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("RAM Gauge", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text("7.25 GB Total System", fontSize = 10.sp, color = Color.LightGray)
                                Text("${(7423 * ramPct / 100).toInt()} MB Used | ${(7423 * (1 - ramPct/100)).toInt()} MB Free", fontSize = 10.sp, color = textAccent)
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("4459 MB used", fontSize = 10.sp, color = Color.White)
                        Text("2964 MB free", fontSize = 10.sp, color = textAccent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                // CPU Mediatek Dimensity Widget (Matches Page 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "CPU Icon",
                        tint = textAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mediatek Dimensity 7300",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cores Speeds Grid (Speeds fluctuating live!)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val chunkedCores = coreFreqs.chunked(4)
                    chunkedCores.forEachIndexed { rowIndex, cores ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            cores.forEachIndexed { colIndex, speed ->
                                val coreIdx = rowIndex * 4 + colIndex + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .background(Color(0xFF16161F), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Core $coreIdx", fontSize = 8.sp, color = Color.Gray)
                                        Text("$speed Mhz", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textAccent)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                // Internal Storage Progress (Used: 100.9 GB, Total: 108.0 GB, 93%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SdCard,
                            contentDescription = "Internal Storage",
                            tint = textAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Internal Storage", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    }
                    Text("93%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textAccent)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { 0.93f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = textAccent,
                    trackColor = Color(0xFF16161F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Used: 100.9 GB, Total: 108.0 GB",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }

        // Quick Indicators (Sensors / All apps counts)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Sensor indicator",
                        tint = textAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("25", fontSize = 16.sp, fontWeight = FontWeight.Black, color = textPrimary)
                        Text("Sensors Available", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Apps Count",
                        tint = textAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("494", fontSize = 16.sp, fontWeight = FontWeight.Black, color = textPrimary)
                        Text("Total Installed Apps", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Quick footer action navigation matching Bottom Shortcuts (Widgets, Monitor, Tests)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { }
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Widgets", tint = textAccent, modifier = Modifier.size(20.dp))
                    Text("Widgets", fontSize = 10.sp, color = textPrimary)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { }
                ) {
                    Icon(Icons.Default.MonitorHeart, contentDescription = "Monitor", tint = textAccent, modifier = Modifier.size(20.dp))
                    Text("Monitor", fontSize = 10.sp, color = textPrimary)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onNavigateToTests() }
                ) {
                    Icon(Icons.Default.PlaylistAddCheck, contentDescription = "Tests", tint = textAccent, modifier = Modifier.size(20.dp))
                    Text("Tests", fontSize = 10.sp, color = textPrimary)
                }
            }
        }
    }
}

// ==================== 2. DEVICE INFO VIEW ====================
@Composable
fun DeviceView(isHighContrast: Boolean) {
    val items = listOf(
        "Device Name" to "A015",
        "Model" to "A015",
        "Manufacturer" to "Nothing",
        "Device" to "Tetris",
        "Board" to "Tetris",
        "Hardware" to "mt6878",
        "Brand" to "Nothing",
        "Google Advertising ID" to "61495a53-ab42-430d-a1f3-c37b76a7d40",
        "Android Device ID" to "414d7f319a8c5f1d",
        "Hardware Serial" to "unknown",
        "Build Fingerprint" to "Nothing/TetrisIND/Tetris:16/BP2A.250605.031.A3/2604151709:user/release-keys",
        "Device Type" to "GSM",
        "Network Operator" to "airtel",
        "Network Type" to "LTE",
        "USB Debugging" to "On"
    )
    KeyValueDetailsList(title = "DEVICE HARDWARE DETAILS", items = items, isHighContrast = isHighContrast)
}

// ==================== 3. SYSTEM VIEW ====================
@Composable
fun SystemView(isHighContrast: Boolean) {
    val items = listOf(
        "Version Name" to "Android 16 Baklava",
        "API Level" to "36",
        "Build Number" to "B4.1-260415-1709",
        "Build Time" to "15 Apr 2026 14:39:29 GMT+05:30",
        "Build ID" to "BP2A.250605.031.A3",
        "Security Patch Level" to "2026-04-01",
        "Release Date" to "June 10, 2025",
        "Root Access" to "No",
        "System Uptime" to "17:08:33",
        "Seamless Updates" to "Supported",
        "Dynamic Partitions" to "Supported",
        "Project Treble" to "Supported (VNDK 34.0)",
        "Java Runtime" to "Android Runtime 0.9",
        "Java VM" to "ART 2.1.0",
        "Kernel Architecture" to "aarch64",
        "Widevine DRM Vendor" to "com.google.android.widevine",
        "DRM Version" to "18.0.0@341310000",
        "DRM Security Level" to "L1",
        "DRM Algorithms" to "AES/CBC/NoPadding, HmacSHA256"
    )
    KeyValueDetailsList(title = "OPERATING SYSTEM", items = items, isHighContrast = isHighContrast)
}

// ==================== 4. CPU VIEW ====================
@Composable
fun CpuView(isHighContrast: Boolean) {
    val items = listOf(
        "Processor SOC" to "Mediatek Dimensity 7300",
        "Cores Count" to "8",
        "CPU Struct" to "4 x Cortex-A78 @ 2.5 GHz\n4 x Cortex-A55 @ 2.0 GHz",
        "Fabrication Node" to "4 nm (TSMC N4)",
        "Supported ABIs" to "arm64-v8a",
        "GPU Renderer" to "Mali-G615 MC2",
        "GPU Vendor" to "ARM",
        "OpenGL ES version" to "3.2 v1.r44p1",
        "Vulkan Version" to "1.3",
        "Dynamic Frequency Range" to "400 Mhz - 2500 Mhz"
    )
    KeyValueDetailsList(title = "CENTRAL PROCESSING UNIT", items = items, isHighContrast = isHighContrast)
}

// ==================== 5. BATTERY VIEW ====================
@Composable
fun BatteryView(isHighContrast: Boolean) {
    val items = listOf(
        "Current Charge Level" to "37% (Discharging)",
        "Raw Power Output" to "-2.81 Watts",
        "Voltage" to "3.80 Volts",
        "Battery Temperature" to "91.4°F (33.0°C)",
        "Health Assessment" to "Good",
        "Power Source" to "Battery",
        "Technology" to "Li-ion",
        "Total Design Capacity" to "5000.0 mAh"
    )
    
    val textAccent = if (isHighContrast) Color.Yellow else Color(0xFF00E676)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circle battery gauge matching 37%
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            if (isHighContrast) Color.Black else Color(0xFFFFB300).copy(alpha = 0.1f), 
                            CircleShape
                        )
                        .border(3.dp, if (isHighContrast) Color.White else Color(0xFFFFB300), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "37%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "DISCHARGING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighContrast) Color.Yellow else Color(0xFFFFB300)
                        )
                    }
                }
            }
        }
        
        items(items) { (key, value) ->
            KeyValueRow(key = key, value = value)
        }
    }
}

// ==================== 6. SENSORS VIEW (Live Interactive values!) ====================
@Composable
fun SensorsView(isHighContrast: Boolean) {
    // Highly interactive live sensory nodes with LaunchedEffect loops
    var accValues by remember { mutableStateOf(Triple(0.24f, 9.81f, -1.45f)) }
    var magValues by remember { mutableStateOf(Triple(18.2f, -24.5f, -35.1f)) }
    var gyroValues by remember { mutableStateOf(Triple(0.01f, -0.02f, 0.00f)) }
    var lightVal by remember { mutableStateOf(42.5f) }
    var stepsCount by remember { mutableStateOf(8415) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            accValues = Triple(
                0.24f + Random.nextFloat() * 0.1f - 0.05f,
                9.81f + Random.nextFloat() * 0.05f - 0.025f,
                -1.45f + Random.nextFloat() * 0.12f - 0.06f
            )
            magValues = Triple(
                18.2f + Random.nextFloat() * 1.5f - 0.75f,
                -24.5f + Random.nextFloat() * 1.8f - 0.9f,
                -35.1f + Random.nextFloat() * 2.2f - 1.1f
            )
            gyroValues = Triple(
                Random.nextFloat() * 0.06f - 0.03f,
                Random.nextFloat() * 0.06f - 0.03f,
                Random.nextFloat() * 0.04f - 0.02f
            )
            lightVal = (lightVal + Random.nextFloat() * 2f - 1f).coerceIn(10f, 1500f)
            if (Random.nextInt(10) > 8) {
                stepsCount += 1
            }
        }
    }

    val textAccent = if (isHighContrast) Color.Yellow else Color(0xFF00E676)
    val cardBg = if (isHighContrast) Color.Black else Color(0xFF0F0F14)
    val cardBorder = if (isHighContrast) Color.White else Color.White.copy(alpha = 0.12f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "REAL-TIME LIVE SENSOR TELEMETRY",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // 1. Accelerometer Card
        item {
            SensorLiveCard(
                name = "ACCELEROMETER",
                sensorId = "icm4n607_acc",
                vendor = "iven_sense",
                power = "0.001mA",
                valueLabel = "Vector m/s²:",
                output = "X: ${String.format(Locale.US, "%.3f", accValues.first)}   Y: ${String.format(Locale.US, "%.3f", accValues.second)}   Z: ${String.format(Locale.US, "%.3f", accValues.third)}",
                isHighContrast = isHighContrast,
                accentColor = textAccent,
                itemBg = cardBg,
                itemBorder = cardBorder
            )
        }

        // 2. Magnetic Field Card
        item {
            SensorLiveCard(
                name = "MAGNETIC FIELD",
                sensorId = "qmc6308",
                vendor = "qst",
                power = "0.001mA",
                valueLabel = "Fields (μT):",
                output = "X: ${String.format(Locale.US, "%.1f", magValues.first)}   Y: ${String.format(Locale.US, "%.1f", magValues.second)}   Z: ${String.format(Locale.US, "%.1f", magValues.third)}",
                isHighContrast = isHighContrast,
                accentColor = textAccent,
                itemBg = cardBg,
                itemBorder = cardBorder
            )
        }

        // 3. Gyroscope Card
        item {
            SensorLiveCard(
                name = "GYROSCOPE",
                sensorId = "icm4n607_gyro",
                vendor = "iven_sense",
                power = "0.001mA",
                valueLabel = "Rotational Speed (rad/s):",
                output = "X: ${String.format(Locale.US, "%.3f", gyroValues.first)}   Y: ${String.format(Locale.US, "%.3f", gyroValues.second)}   Z: ${String.format(Locale.US, "%.3f", gyroValues.third)}",
                isHighContrast = isHighContrast,
                accentColor = textAccent,
                itemBg = cardBg,
                itemBorder = cardBorder
            )
        }

        // 4. Ambient Light Card
        item {
            SensorLiveCard(
                name = "LIGHT FLUX SENSOR",
                sensorId = "ltr569_als",
                vendor = "liteOn",
                power = "0.001mA",
                valueLabel = "Lux Intensity:",
                output = "${String.format(Locale.US, "%.1f", lightVal)} lx (Flux)",
                isHighContrast = isHighContrast,
                accentColor = textAccent,
                itemBg = cardBg,
                itemBorder = cardBorder
            )
        }

        // 5. Step Detector Card
        item {
            SensorLiveCard(
                name = "STEP COUNTER INDICATOR",
                sensorId = "step_count",
                vendor = "mtk",
                power = "0.001mA",
                valueLabel = "Pedometer Records:",
                output = "$stepsCount cumulative steps",
                isHighContrast = isHighContrast,
                accentColor = textAccent,
                itemBg = cardBg,
                itemBorder = cardBorder
            )
        }
    }
}

@Composable
fun SensorLiveCard(
    name: String,
    sensorId: String,
    vendor: String,
    power: String,
    valueLabel: String,
    output: String,
    isHighContrast: Boolean,
    accentColor: Color,
    itemBg: Color,
    itemBorder: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, itemBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = itemBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Text(
                    text = "ID: $sensorId",
                    fontSize = 8.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Vendor: $vendor", fontSize = 9.sp, color = Color.Gray)
                Text("|", fontSize = 9.sp, color = Color.Gray)
                Text("Power Draw: $power", fontSize = 9.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16161F), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text(valueLabel, fontSize = 8.sp, color = Color.Gray)
                    Text(
                        text = output,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== 7. APPS DIAGNOSTIC VIEW ====================
data class HardwareAppSpec(
    val name: String,
    val packageName: String,
    val targetApi: Int,
    val is64Bit: Boolean,
    val securityLevel: String
)

@Composable
fun AppsDiagnosticView(isHighContrast: Boolean) {
    val mockApps = listOf(
        HardwareAppSpec("Admin Services", "com.google.android.apps.enterprise.cpanel", 36, true, "Encrypted / Verified"),
        HardwareAppSpec("MetaMask", "io.metamask", 35, true, "Sandbox Sandbox Verified"),
        HardwareAppSpec("SwiftKey Keyboard", "com.touchtype.swiftkey", 36, true, "Input Connection OK"),
        HardwareAppSpec("Mimo Developer", "com.getmimo", 36, true, "Local Compilation Secure"),
        HardwareAppSpec("MiniMax Machine", "com.minimax.ai", 35, true, "AI Isolation Sandbox"),
        HardwareAppSpec("Airtel Network Support", "com.myairtelapp", 35, true, "Verified Telecom Access"),
        HardwareAppSpec("AhaTik Downloader", "tiktok.video.downloader.nowatermark", 35, true, "Inspected (No Adware)")
    )

    var appsScanResult by remember { mutableStateOf<String?>(null) }
    var isAppsScanning by remember { mutableStateOf(false) }

    val accentColor = if (isHighContrast) Color.Yellow else Color(0xFF00E676)
    val cardBg = if (isHighContrast) Color.Black else Color(0xFF0F0F14)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Installed Packages Inspection", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Verify target API levels and isolated sandbox bounds", fontSize = 9.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            isAppsScanning = true
                            appsScanResult = null
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("apps_sec_scan_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                            contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("ANALYZE SENSITIVE PACKAGES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (isAppsScanning) {
            item {
                LaunchedEffect(Unit) {
                    delay(1500)
                    isAppsScanning = false
                    appsScanResult = "✅ All 7 sensitive packaging processes were evaluated. Standard 64-bit bounds are strictly verified. Sandbox constraints: ACTIVE. 0 vulnerabilities or package leaks discovered!"
                }
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = accentColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Auditing package target flags...", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        appsScanResult?.let { result ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F321B), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(result, color = Color.White, fontSize = 11.sp)
                }
            }
        }

        items(mockApps) { app ->
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(app.packageName, fontSize = 9.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("API Flag: ${app.targetApi}", fontSize = 8.sp, color = accentColor)
                            Text("|", fontSize = 8.sp, color = Color.DarkGray)
                            Text("Scope: ${app.securityLevel}", fontSize = 8.sp, color = Color.LightGray)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF16161F), RoundedCornerShape(4.dp))
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (app.is64Bit) "64-BIT" else "32-BIT",
                            fontSize = 8.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==================== 8. DIAGNOSTIC HARDWARE TESTS (Interactive) ====================
@Composable
fun DiagnosticTestsView(isHighContrast: Boolean) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    // State Variables for Interactive Diagnostics
    var flashlightState by remember { mutableStateOf(false) }
    var scaleAnimation = remember { Animatable(1f) }
    
    var isVibratingSimulated by remember { mutableStateOf(false) }
    var screenColorOverlay by remember { mutableStateOf<Color?>(null) }
    
    var proximityDistance by remember { mutableStateOf(10.0f) } // Proximity distance swipe
    
    var diagnosticBeepActive by remember { mutableStateOf(false) }
    
    var volumeUpClicked by remember { mutableStateOf(false) }
    var volumeDownClicked by remember { mutableStateOf(false) }

    val accentColor = if (isHighContrast) Color.Yellow else Color(0xFF00E676)
    val cardBg = if (isHighContrast) Color.Black else Color(0xFF0F0F14)
    val cardBorder = if (isHighContrast) Color.White else Color.White.copy(alpha = 0.1f)

    if (screenColorOverlay != null) {
        // Fullscreen color diagnostic view overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenColorOverlay!!)
                .clickable {
                    // Cycles screen colors: Red -> Green -> Blue -> White -> Dismiss
                    screenColorOverlay = when (screenColorOverlay) {
                        Color.Red -> Color.Green
                        Color.Green -> Color.Blue
                        Color.Blue -> Color.White
                        else -> null
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "Display Pixel Test Mode", 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tap to cycle colors (R -> G -> B -> W).", 
                    color = Color.LightGray, 
                    fontSize = 11.sp
                )
                Text(
                    "Press anywhere to cycle / dismiss", 
                    color = accentColor, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "CLICK CHANNELS TO OPERATE DIAGNOSTICS UNIT",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // Test 1: Flashlight Toggle Test (Interactive blinking!)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simulated Flashlight", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (flashlightState) "FLASHLIGHT STATE: PULSING ON" else "Evaluate rear LED circuit",
                            fontSize = 10.sp,
                            color = if (flashlightState) accentColor else Color.Gray,
                            fontWeight = if (flashlightState) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (flashlightState) accentColor.copy(alpha = 0.3f) else Color(0xFF16161F), 
                                CircleShape
                            )
                            .border(1.dp, if (flashlightState) accentColor else Color.DarkGray, CircleShape)
                            .clickable { flashlightState = !flashlightState },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Flashlight Toggle",
                            tint = if (flashlightState) accentColor else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Test 2: Simulated Vibration Pulse Test
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vibrator Actuator Haptics", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (isVibratingSimulated) "📳 VIBRATING..." else "Test motor raw tactile indices",
                            fontSize = 10.sp,
                            color = if (isVibratingSimulated) accentColor else Color.Gray,
                            fontWeight = if (isVibratingSimulated) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Button(
                        onClick = {
                            isVibratingSimulated = true
                            try {
                                vibrator?.vibrate(200L)
                            } catch (e: Exception) { /* safe fallback */ }
                        },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVibratingSimulated) accentColor else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isVibratingSimulated) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        LaunchedEffect(isVibratingSimulated) {
                            if (isVibratingSimulated) {
                                delay(800)
                                isVibratingSimulated = false
                            }
                        }
                        Text("PULSE MOTOR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Test 3: Large Screen Pixel Dead Color Test Overlay
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Display Color dead pixel evaluation", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Launches color canvas grids to examine displays", fontSize = 10.sp, color = Color.Gray)
                    }

                    IconButton(
                        onClick = { screenColorOverlay = Color.Red },
                        modifier = Modifier
                            .background(Color(0xFF16161F), CircleShape)
                            .border(1.dp, Color.DarkGray, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Color grid launch",
                            tint = accentColor
                        )
                    }
                }
            }
        }

        // Test 4: Ear Proximity Interactive Slide Bar Test (Simulates proximity sensor!)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Proximity Hand-Wave distance", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (proximityDistance < 3f) "⚠️ NEAR (0.0 cm)" else "FAR (10.0 cm)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (proximityDistance < 3f) Color.Red else accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Drag sensor obstacle to mock device ear distance proximity change:", fontSize = 9.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = proximityDistance,
                        onValueChange = { 
                            proximityDistance = it
                            if (it < 3f) {
                                try { vibrator?.vibrate(50L) } catch (e: Exception) {}
                            }
                        },
                        valueRange = 0.0f..10.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (proximityDistance < 3f) Color.Red else accentColor,
                            activeTrackColor = if (proximityDistance < 3f) Color.Red else accentColor,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                }
            }
        }

        // Test 5: Loudspeaker Frequency Speaker Diagnostic Sound test
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Loudspeaker Sweep Diagnostic", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (diagnosticBeepActive) "🔊 EMITTING 440HZ REFERENCE SINE WAVE..." else "Audible tone amplifier diagnostic beep",
                            fontSize = 10.sp,
                            color = if (diagnosticBeepActive) accentColor else Color.Gray,
                            fontWeight = if (diagnosticBeepActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Button(
                        onClick = {
                            diagnosticBeepActive = true
                        },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (diagnosticBeepActive) accentColor else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (diagnosticBeepActive) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        LaunchedEffect(diagnosticBeepActive) {
                            if (diagnosticBeepActive) {
                                delay(1200)
                                diagnosticBeepActive = false
                            }
                        }
                        Text("SOUND BEEP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Test 6: Volume Buttons Physical Registries simulation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Volume Hardware Buttons Test", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Audit click responses of volume circuits. Click simulated switches:", fontSize = 9.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { volumeUpClicked = true },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (volumeUpClicked) Color(0xFF0F321B) else Color(0xFF16161F)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (volumeUpClicked) Color(0xFF00E676) else Color.DarkGray)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("VOLUME UP: ", fontSize = 9.sp, color = Color.White)
                                Text(
                                    text = if (volumeUpClicked) "PASS ✅" else "TEST UNIT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (volumeUpClicked) Color(0xFF00E676) else Color.Gray
                                )
                            }
                        }

                        Button(
                            onClick = { volumeDownClicked = true },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (volumeDownClicked) Color(0xFF0F321B) else Color(0xFF16161F)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (volumeDownClicked) Color(0xFF00E676) else Color.DarkGray)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("VOLUME DOWN: ", fontSize = 9.sp, color = Color.White)
                                Text(
                                    text = if (volumeDownClicked) "PASS ✅" else "TEST UNIT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (volumeDownClicked) Color(0xFF00E676) else Color.Gray
                                )
                            }
                        }
                    }
                    if (volumeUpClicked || volumeDownClicked) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = {
                                volumeUpClicked = false
                                volumeDownClicked = false
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Reset Button Diagnostics", color = accentColor, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

// Helper components for Key-Value tables
@Composable
fun KeyValueDetailsList(
    title: String,
    items: List<Pair<String, String>>,
    isHighContrast: Boolean
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        items.forEach { (key, value) ->
            KeyValueRow(key = key, value = value)
        }
    }
}

@Composable
fun KeyValueRow(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Color.White.copy(alpha = 0.03f),
                RoundedCornerShape(6.dp)
            )
            .background(Color(0xFF0A0A0F), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = key,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.1f)
        )
    }
}
