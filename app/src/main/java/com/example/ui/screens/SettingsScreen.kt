package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OptimiserViewModel

@Composable
fun SettingsScreen(
    viewModel: OptimiserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val isHighContrast by viewModel.isHighContrast.collectAsState()
    val customKey by viewModel.customApiKey.collectAsState()

    var keyInput by remember { mutableStateOf(customKey) }

    val activeBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.background
    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onBackground
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
    val activeBorderColor = if (isHighContrast) Color.White else Color.Transparent
    val cardBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(activeBgColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settings Section Header
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = "", tint = activeTextColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "APPLICATION SETTINGS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    color = activeTextColor
                )
            }
            Text(
                text = "Manage system themes, credentials, API endpoints, and clean data logs",
                style = MaterialTheme.typography.bodySmall,
                color = activeSubTextColor
            )
        }

        // --- 1. Accessibility Controls card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, activeBorderColor, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "THEMING & ACCESSIBILITY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = activeTextColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Dark mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = "", tint = activeTextColor)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Dark Theme Mode", fontSize = 13.sp, color = activeTextColor, fontWeight = FontWeight.SemiBold)
                            Text("Render AMOLED pitch dark aesthetic", fontSize = 10.sp, color = activeSubTextColor)
                        }
                    }

                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.testTag("dark_theme_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High Contrast accessibility toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessibilityNew, contentDescription = "", tint = activeTextColor)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("High Contrast Accessibility", fontSize = 13.sp, color = activeTextColor, fontWeight = FontWeight.SemiBold)
                            Text("Max readability with monochrome borders & high saturation", fontSize = 10.sp, color = activeSubTextColor)
                        }
                    }

                    Switch(
                        checked = isHighContrast,
                        onCheckedChange = { viewModel.toggleHighContrast() },
                        modifier = Modifier.testTag("high_contrast_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // --- 2. Advanced Custom Gemini Key Configuration ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, activeBorderColor, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = "", tint = activeTextColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GEMINI FREE TIER CUSTOM KEY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = activeTextColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "If you want to use your individual Gemini Free Tier quotas, enter your private Google API key below. Empty fields automatically slide-back to our pre-configured backend token.",
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = activeSubTextColor
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Gemini API Key", color = activeTextColor) },
                    placeholder = { Text("AIzaSy...", color = activeSubTextColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = activeTextColor,
                        unfocusedTextColor = activeTextColor,
                        focusedBorderColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.setCustomApiKey(keyInput)
                        Toast.makeText(context, "Gemini API Key persistent configuration updated!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_api_key_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                        contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("UPDATE GEMINI API KEY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- 3. Danger Zone Logs data purge ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isHighContrast) Color.White else Color.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isHighContrast) Color.Black else Color.Red.copy(alpha = 0.04f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "",
                        tint = if (isHighContrast) Color.White else Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DATA PURGE DIRECTIVE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isHighContrast) Color.White else Color.Red
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Permanently wipes all historical audit records and optimization logs from your local SQLite device memory. This operation is irreversible.",
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = activeSubTextColor
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.clearLogs()
                        Toast.makeText(context, "All history audit logs purged from system!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("wipe_db_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) Color.White else Color.Red,
                        contentColor = if (isHighContrast) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("WIPE DATABASE HISTORY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}
