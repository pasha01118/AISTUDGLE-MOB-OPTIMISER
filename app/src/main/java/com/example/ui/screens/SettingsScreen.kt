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
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import com.example.viewmodel.OptimiserViewModel

@Composable
fun SettingsScreen(
    viewModel: OptimiserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val isHighContrast by viewModel.isHighContrast.collectAsState()
    val showSentinelFloatingButton by viewModel.showSentinelFloatingButton.collectAsState()
    
    // --- Dynamic Multi-provider AI key flows ---
    val activeProvider by viewModel.selectedAiProvider.collectAsState()
    val geminiKey by viewModel.geminiApiKey.collectAsState()
    val openaiKey by viewModel.openaiApiKey.collectAsState()
    val openrouterKey by viewModel.openrouterApiKey.collectAsState()
    val ollamaUrl by viewModel.ollamaEndpoint.collectAsState()
    val ollamaName by viewModel.ollamaModel.collectAsState()
    val grokKey by viewModel.grokApiKey.collectAsState()
    val hfToken by viewModel.huggingfaceToken.collectAsState()
    val hfModel by viewModel.huggingfaceModel.collectAsState()
    val hfValidationResult by viewModel.hfValidationResult.collectAsState()
    val isHfValidating by viewModel.isHfValidating.collectAsState()
    val mistralKey by viewModel.mistralApiKey.collectAsState()
    val qwenKey by viewModel.qwenApiKey.collectAsState()
    val veniceKey by viewModel.veniceApiKey.collectAsState()
    val veniceModel by viewModel.veniceModel.collectAsState()
    val customUrl by viewModel.customAiEndpoint.collectAsState()
    val customKey by viewModel.customAiApiKey.collectAsState()
    val customName by viewModel.customAiModel.collectAsState()

    var showAiModal by remember { mutableStateOf(false) }

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

                Spacer(modifier = Modifier.height(16.dp))

                // Floating Sentinel FAB toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = "", tint = activeTextColor)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Show Floating Sentinel", fontSize = 13.sp, color = activeTextColor, fontWeight = FontWeight.SemiBold)
                            Text("Display launcher FAB overlay on Optimise Dashboard", fontSize = 10.sp, color = activeSubTextColor)
                        }
                    }

                    Switch(
                        checked = showSentinelFloatingButton,
                        onCheckedChange = { viewModel.toggleSentinelFloatingButton() },
                        modifier = Modifier.testTag("sentinel_fab_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // --- 2. Advanced Multi-provider AI Channels Engine Config ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, activeBorderColor, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "", tint = activeTextColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE CLOUD AI COMPILER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = activeTextColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Selected Provider",
                            fontSize = 10.sp,
                            color = activeSubTextColor
                        )
                        Text(
                            text = activeProvider,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Free Tier Connected",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Unlock agentic control across 9 leading AI networks (such as OpenRouter with unlimited free llama3 endpoints, Hugging Face Serverless APIs, mistral, Grok, Qwen, etc.). Configure distinct keys & API boundaries below.",
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = activeSubTextColor
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showAiModal = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("open_ai_modal_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                        contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CONFIGURE AI CHANNELS (9 PLATFORMS)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- 9-Platform AI API Settings Dialog Modal ---
        if (showAiModal) {
            var localProvider by remember { mutableStateOf(activeProvider) }
            var localGeminiKey by remember { mutableStateOf(geminiKey) }
            var localOpenaiKey by remember { mutableStateOf(openaiKey) }
            var localOpenrouterKey by remember { mutableStateOf(openrouterKey) }
            var localOllamaUrl by remember { mutableStateOf(ollamaUrl) }
            var localOllamaName by remember { mutableStateOf(ollamaName) }
            var localGrokKey by remember { mutableStateOf(grokKey) }
            var localHfToken by remember { mutableStateOf(hfToken) }
            var localHfModel by remember { mutableStateOf(hfModel) }
            var localMistralKey by remember { mutableStateOf(mistralKey) }
            var localQwenKey by remember { mutableStateOf(qwenKey) }
            var localVeniceKey by remember { mutableStateOf(veniceKey) }
            var localVeniceModel by remember { mutableStateOf(veniceModel) }
            var localCustomUrl by remember { mutableStateOf(customUrl) }
            var localCustomKey by remember { mutableStateOf(customKey) }
            var localCustomName by remember { mutableStateOf(customName) }

            AlertDialog(
                onDismissRequest = { showAiModal = false },
                title = {
                    Text(
                        "Configure AI APIs Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = activeTextColor
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Select active provider and input API keys below. Free tiers/unlimited free models with no caps (e.g. llama free on OpenRouter) are recommended.",
                            fontSize = 11.sp,
                            color = activeSubTextColor
                        )

                        // Providers Row
                        Text(
                            text = "ACTIVE CHANNELS LIST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeTextColor
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val listProviders = listOf(
                                "Optina Agent", "Gemini", "OpenAI", "OpenRouter", "Ollama Cloud",
                                "Grok AI (xAI)", "Hugging Face", "Mistral API", "Qwen AI", "Venice AI", "Offline Small Model (Simulation)", "Custom Free"
                            )
                            listProviders.forEach { p ->
                                val isSel = localProvider == p
                                Card(
                                    modifier = Modifier.clickable { localProvider = p },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSel) {
                                            if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
                                        } else {
                                            if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = if (isHighContrast) androidx.compose.foundation.BorderStroke(1.dp, Color.White) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSel) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "selected",
                                                tint = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = p,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) {
                                                if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                activeTextColor
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Dynamic Section Info and Fields
                        when (localProvider) {
                            "Optina Agent" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Optina Agent acts as an autonomous aggregator under your behavioural command. It routes queries dynamically to Gemini/OpenAI if their keys are configured, or automatically falls back to offline models.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }
                            }
                            "Offline Small Model (Simulation)" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Compiles zero-network, local, offline model layers like Qwen 1B or Tony Lama under localized hardware weights.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }
                            }
                            "Gemini" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Google Gemini AI Studio API key provides an unlimited/high-limit free tier for developer prototyping.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = localGeminiKey,
                                    onValueChange = { localGeminiKey = it },
                                    label = { Text("Gemini API Key", fontSize = 11.sp) },
                                    placeholder = { Text("AIzaSy...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_gemini_key"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )
                            }
                            "OpenAI" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Supports standard OpenAI key credentials. Prompts are constructed and routed safely to 'gpt-4o-mini'.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = localOpenaiKey,
                                    onValueChange = { localOpenaiKey = it },
                                    label = { Text("OpenAI API Key", fontSize = 11.sp) },
                                    placeholder = { Text("sk-...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_openai_key"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )
                            }
                            "OpenRouter" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Unlimited cost-free models! Use the free tier key from openrouter.ai/keys to access unlimited models with no strict hourly limits (llama-3-8b-instruct:free).",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = localOpenrouterKey,
                                    onValueChange = { localOpenrouterKey = it },
                                    label = { Text("OpenRouter API Key", fontSize = 11.sp) },
                                    placeholder = { Text("sk-or-...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_openrouter_key"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )
                            }
                            "Ollama Cloud" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Run completely free, custom, zero-internet LLMs locally on your desktop/server using Ollama's direct endpoints.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = localOllamaUrl,
                                    onValueChange = { localOllamaUrl = it },
                                    label = { Text("Ollama Server URL", fontSize = 11.sp) },
                                    placeholder = { Text("http://10.0.2.2:11434", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_ollama_url"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )

                                OutlinedTextField(
                                    value = localOllamaName,
                                    onValueChange = { localOllamaName = it },
                                    label = { Text("Ollama Model Name", fontSize = 11.sp) },
                                    placeholder = { Text("llama3", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_ollama_model"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )
                            }
                            "Grok AI (xAI)" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Configure xAI's console.x.ai API keys for ultra-fast, structured device optimization loops.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = localGrokKey,
                                    onValueChange = { localGrokKey = it },
                                    label = { Text("Grok API Key", fontSize = 11.sp) },
                                    placeholder = { Text("xai-...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_grok_key"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )
                            }
                            "Hugging Face" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Completely free serverless inference API! Run tens of thousands of LLMs on hf.co using your free profile token.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = localHfToken,
                                    onValueChange = { localHfToken = it },
                                    label = { Text("Hugging Face User Token (hf_...)", fontSize = 11.sp) },
                                    placeholder = { Text("hf_...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_hf_token"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )

                                OutlinedTextField(
                                    value = localHfModel,
                                    onValueChange = { localHfModel = it },
                                    label = { Text("HF Inference Model Repo Path", fontSize = 11.sp) },
                                    placeholder = { Text("meta-llama/Llama-3.2-1B-Instruct", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_hf_model"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )

                                Button(
                                    onClick = { viewModel.validateHuggingFaceToken(localHfToken) },
                                    enabled = !isHfValidating,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isHighContrast) Color.Yellow else Color(0xFFFF9800),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .testTag("validate_hf_token_button")
                                ) {
                                    if (isHfValidating) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("VALIDATING...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("TEST & VALIDATE TOKEN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                hfValidationResult?.let { result ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (result.contains("SUCCESS")) Color(0xFF1B5E20) else Color(0xFFC62828)
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                             .testTag("hf_validation_result_card")
                                    ) {
                                        Text(
                                            text = result,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                            "Mistral API" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "France's leading open-weights AI. Maps directly to standard open-mistral-7b endpoints.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = localMistralKey,
                                    onValueChange = { localMistralKey = it },
                                    label = { Text("Mistral API Key", fontSize = 11.sp) },
                                    placeholder = { Text("mistral-...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_mistral_key"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )
                            }
                            "Qwen AI" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Alibaba DashScope provides multi-million tokens on free tiers to access state-of-the-art Qwen LLMs.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = localQwenKey,
                                    onValueChange = { localQwenKey = it },
                                    label = { Text("Qwen / Alibaba DashScope Key", fontSize = 11.sp) },
                                    placeholder = { Text("sk-...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_qwen_key"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )
                            }
                            "Venice AI" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Venice.ai provides high-performance uncensored open-weights inference. Support customizable models under standard OpenAI schemas.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = localVeniceKey,
                                    onValueChange = { localVeniceKey = it },
                                    label = { Text("Venice API Key", fontSize = 11.sp) },
                                    placeholder = { Text("Enter your Venice Token", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_venice_key"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )

                                OutlinedTextField(
                                    value = localVeniceModel,
                                    onValueChange = { localVeniceModel = it },
                                    label = { Text("Venice Model Name", fontSize = 11.sp) },
                                    placeholder = { Text("venice-uncensored", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_venice_model"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )
                            }
                            "Custom Free" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, "", tint = activeTextColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Input any OpenAI-compatible third-party or free custom proxy endpoint with no strict request limits.",
                                            fontSize = 9.sp,
                                            color = activeSubTextColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = localCustomUrl,
                                    onValueChange = { localCustomUrl = it },
                                    label = { Text("Target Base URL Endpoint", fontSize = 11.sp) },
                                    placeholder = { Text("https://api.free-gateway.org/v1/chat/completions", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_custom_url"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )

                                OutlinedTextField(
                                    value = localCustomKey,
                                    onValueChange = { localCustomKey = it },
                                    label = { Text("API Key (leave empty if none)", fontSize = 11.sp) },
                                    placeholder = { Text("sk-optional...", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_custom_key"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )

                                OutlinedTextField(
                                    value = localCustomName,
                                    onValueChange = { localCustomName = it },
                                    label = { Text("Target Model Name", fontSize = 11.sp) },
                                    placeholder = { Text("llama3", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("modal_custom_model"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = activeTextColor,
                                        unfocusedTextColor = activeTextColor
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateAiConfiguration(
                                provider = localProvider,
                                gemini = localGeminiKey,
                                openai = localOpenaiKey,
                                openrouter = localOpenrouterKey,
                                ollamaUrl = localOllamaUrl,
                                ollamaName = localOllamaName,
                                grok = localGrokKey,
                                hfToken = localHfToken,
                                hfModel = localHfModel,
                                mistral = localMistralKey,
                                qwen = localQwenKey,
                                customUrl = localCustomUrl,
                                customKey = localCustomKey,
                                customName = localCustomName,
                                venice = localVeniceKey,
                                veniceModel = localVeniceModel
                            )
                            showAiModal = false
                            Toast.makeText(context, "AI Configuration updated to $localProvider!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                            contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("modal_save_button")
                    ) {
                        Text("SAVE CHANGES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showAiModal = false },
                        modifier = Modifier.testTag("modal_cancel_button")
                    ) {
                        Text("CANCEL", color = activeTextColor, fontSize = 11.sp)
                    }
                },
                containerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
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
