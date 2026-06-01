package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.OptimiserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    viewModel: OptimiserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Core state flows
    val messages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val isHighContrast by viewModel.isHighContrast.collectAsState()
    
    // Optina Agent-specific flows
    val isAiAgentEnabled by viewModel.isAiAgentEnabled.collectAsState()
    val selectedAiProvider by viewModel.selectedAiProvider.collectAsState()
    val agentLearningScore by viewModel.agentLearningScore.collectAsState()
    val agentLearnedSpecs by viewModel.agentLearnedSpecs.collectAsState()
    val offlineModelsList by viewModel.offlineModelsList.collectAsState()
    val selectedOfflineModel by viewModel.selectedOfflineModel.collectAsState()

    // Jamun Agent Local variables
    val jamunAgentName by viewModel.jamunAgentName.collectAsState()
    val jamunAgentModelUrl by viewModel.jamunAgentModelUrl.collectAsState()
    val jamunAgentArchetype by viewModel.jamunAgentArchetype.collectAsState()
    val isCpuOverclocked by viewModel.isCpuOverclocked.collectAsState()
    val isThermalsCooled by viewModel.isThermalsCooled.collectAsState()
    val isUiLagResolved by viewModel.isUiLagResolved.collectAsState()

    var showConfigPanel by remember { mutableStateOf(false) }
    var editAgentName by remember { mutableStateOf("") }
    var editModelUrl by remember { mutableStateOf("") }
    var editArchetype by remember { mutableStateOf("") }

    LaunchedEffect(jamunAgentName, jamunAgentModelUrl, jamunAgentArchetype) {
        editAgentName = jamunAgentName
        editModelUrl = jamunAgentModelUrl
        editArchetype = jamunAgentArchetype
    }

    val isAutonomousMode by viewModel.isAutonomousMode.collectAsState()
    val isJamunDownloaded by viewModel.isJamunDownloaded.collectAsState()
    val isJamunDownloading by viewModel.isJamunDownloading.collectAsState()
    val jamunDownloadProgress by viewModel.jamunDownloadProgress.collectAsState()
    val showJamunGreeting by viewModel.showJamunGreeting.collectAsState()
    val jamunAuditStep by viewModel.jamunAuditStep.collectAsState()
    val isJamunAuditing by viewModel.isJamunAuditing.collectAsState()
    val jamunAuditLogs by viewModel.jamunAuditLogs.collectAsState()
    val hasFirstGreetingBeenAllowed by viewModel.hasFirstGreetingBeenAllowed.collectAsState()

    val jamunStep1Allowed by viewModel.jamunStep1Allowed.collectAsState()
    val jamunStep2Allowed by viewModel.jamunStep2Allowed.collectAsState()
    val jamunStep3Allowed by viewModel.jamunStep3Allowed.collectAsState()
    val jamunStep4Allowed by viewModel.jamunStep4Allowed.collectAsState()
    val jamunStep5Allowed by viewModel.jamunStep5Allowed.collectAsState()
    
    // UI Local state
    var textInputState by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var isBrowseActive by remember { mutableStateOf(false) }
    var learningHubExpanded by remember { mutableStateOf(false) }
    
    // Attachment simulations
    var attachedDocument by remember { mutableStateOf<String?>(null) }
    var attachedImage by remember { mutableStateOf<String?>(null) }
    
    // Custom model dialog state
    var showAddModelDialog by remember { mutableStateOf(false) }
    var newModelName by remember { mutableStateOf("") }
    var newModelSize by remember { mutableStateOf("1.2 GB") }
    var newModelParams by remember { mutableStateOf("1.5 B") }
    
    // Voice simulation
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceRecordTimer by remember { mutableStateOf(3) }
    var simulatedVoiceTranscript by remember { mutableStateOf("") }
    
    // Multi-step thinking simulation states
    var currentThinkingStep by remember { mutableStateOf("Optina starting analyzer...") }
    
    val listState = rememberLazyListState()

    // Auto scroll chat list down on new message payload
    LaunchedEffect(messages.size, isChatLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Step shifter for AI Thinking indicator
    LaunchedEffect(isChatLoading) {
        if (isChatLoading) {
            val steps = listOf(
                "Optina starting analyzer...",
                "Evaluating behavioral parameters on target hardware...",
                "Aggregating models with low-latency fallback routing...",
                "Optimizing system state instruction weights...",
                "Compiling responsive local command instructions..."
            )
            var idx = 0
            while (isChatLoading) {
                currentThinkingStep = steps[idx % steps.size]
                delay(1200)
                idx++
            }
        }
    }

    val activeBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.background
    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onBackground
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
    val activeBorderColor = if (isHighContrast) Color.White else Color.Transparent
    val cardBg = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val dividerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(activeBgColor)
    ) {
        // --- 1. GMail Secure OAuth panel (Top Sticky) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(1.dp, activeBorderColor, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (userProfile == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Red.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Google GMail API Sync",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeTextColor
                                )
                                Text(
                                    "Unlock multi-device snapshot storage",
                                    fontSize = 10.sp,
                                    color = activeSubTextColor
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.signInWithGmailOAuth() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHighContrast) Color.White else Color.Red,
                                contentColor = if (isHighContrast) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("gmail_sign_in_button")
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = if (isHighContrast) Color.Black else Color.White
                                )
                            } else {
                                Icon(Icons.Default.Login, contentDescription = "", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GMail OAuth", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "GMail Account profile", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    userProfile!!.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeTextColor
                                )
                                Text(
                                    userProfile!!.email,
                                    fontSize = 11.sp,
                                    color = activeSubTextColor
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.signOutGmail() },
                            modifier = Modifier.testTag("gmail_sign_out_button")
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Log out from Google",
                                tint = if (isHighContrast) Color.White else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "",
                            tint = if (isHighContrast) Color.White else Color(0xFF4CAF50),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Authenticated via GMail sync. Multi-device backups live.",
                            fontSize = 9.sp,
                            color = activeSubTextColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- 2. Perplexity Header: Active Model Dropdown & Switch Toggle ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Dropdown selector for Providers
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isHighContrast) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .clickable { dropdownExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selectedAiProvider == "Optina Agent") Icons.Default.SmartToy else Icons.Default.AutoAwesome,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedAiProvider,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 140.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown icon",
                                tint = activeTextColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            val providers = listOf(
                                "Optina Agent", "Gemini", "OpenAI", "OpenRouter", 
                                "Ollama Cloud", "Grok AI (xAI)", "Hugging Face", 
                                "Mistral API", "Qwen AI", "Venice AI", "Offline Small Model (Simulation)", "Custom Free"
                            )
                            providers.forEach { prov ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(prov, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                            if (selectedAiProvider == prov) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(Icons.Default.Check, contentDescription = "Current selected", tint = Color.Green, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.updateAiConfiguration(
                                            provider = prov,
                                            gemini = viewModel.geminiApiKey.value,
                                            openai = viewModel.openaiApiKey.value,
                                            openrouter = viewModel.openrouterApiKey.value,
                                            ollamaUrl = viewModel.ollamaEndpoint.value,
                                            ollamaName = viewModel.ollamaModel.value,
                                            grok = viewModel.grokApiKey.value,
                                            hfToken = viewModel.huggingfaceToken.value,
                                            hfModel = viewModel.huggingfaceModel.value,
                                            mistral = viewModel.mistralApiKey.value,
                                            qwen = viewModel.qwenApiKey.value,
                                            customUrl = viewModel.customAiEndpoint.value,
                                            customKey = viewModel.customAiApiKey.value,
                                            customName = viewModel.customAiModel.value,
                                            venice = viewModel.veniceApiKey.value,
                                            veniceModel = viewModel.veniceModel.value
                                        )
                                        dropdownExpanded = false
                                        Toast.makeText(context, "Provider altered: $prov", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    // Right Toggle Selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isAiAgentEnabled) "Optina: ON" else "Optina: OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAiAgentEnabled) MaterialTheme.colorScheme.primary else activeSubTextColor,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Switch(
                            checked = isAiAgentEnabled,
                            onCheckedChange = { viewModel.setAiAgentEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.scale(0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(4.dp))

                // Mode metrics banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isAiAgentEnabled) Color.Green else Color.Gray, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!isAiAgentEnabled) "Raw Manual Control Enabled"
                                   else if (selectedAiProvider == "Optina Agent") "Autonomous Aggregator Active"
                                   else "Manual Co-pilot Mode",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = activeSubTextColor
                        )
                    }

                    // Internet search toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isBrowseActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { isBrowseActive = !isBrowseActive }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = "Globe Icon",
                            tint = if (isBrowseActive) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Browse Web",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBrowseActive) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }

        // --- JAMUN DEEP AGENTIC CYBERPUNK HUD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .border(2.dp, Color(0xFFFF3D00).copy(alpha = 0.8f), RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0E15)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isJamunDownloaded) Color.Green else if (isJamunDownloading) Color(0xFFFFB300) else Color.Red,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🦾 ${jamunAgentName.uppercase()} AGENT HUD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (showConfigPanel) Color(0xFFFF5722).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.clickable { showConfigPanel = !showConfigPanel }
                        ) {
                            Text(
                                text = "IDENTITY",
                                fontSize = 8.sp,
                                color = if (showConfigPanel) Color(0xFFFFAB91) else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAutonomousMode) Color(0xFFE53935).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.clickable { viewModel.setAutonomousMode(!isAutonomousMode) }
                        ) {
                            Text(
                                text = if (isAutonomousMode) "FULLY AUTONOMOUS" else "MANUAL CO-PILOT",
                                fontSize = 8.sp,
                                color = if (isAutonomousMode) Color(0xFFFF8A80) else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(8.dp))

                if (showConfigPanel) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A29)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🔧 CONFIGURE HF AGENT IDENTITY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Cyan,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = editAgentName,
                                onValueChange = { editAgentName = it },
                                label = { Text("Agent Identity Name", fontSize = 10.sp) },
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Cyan,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = Color.Cyan
                                ),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = editModelUrl,
                                onValueChange = { editModelUrl = it },
                                label = { Text("Hugging Face GGUF weights URL", fontSize = 10.sp) },
                                textStyle = LocalTextStyle.current.copy(fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Cyan,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = Color.Cyan
                                ),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Popular Presets GGUF weights (Tap to load):", fontSize = 8.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val presets = listOf(
                                    "Qwen 1.5B" to "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q3_k_m.gguf",
                                    "Llama 1B" to "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
                                    "Gemma 2B" to "https://huggingface.co/google/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf",
                                    "Phi-3" to "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf"
                                )
                                presets.forEach { (lbl, url) ->
                                    Button(
                                        onClick = { editModelUrl = url },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.weight(1f).height(24.dp)
                                    ) {
                                        Text(lbl, fontSize = 8.sp, color = Color.LightGray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Agent Mission Archetype:", fontSize = 8.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            val archetypes = listOf(
                                "Hyper-Optimizer System Watcher",
                                "Sentient Silicon Lifeform",
                                "Cyberpunk Shadow Operator",
                                "Tactical System Shield"
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                archetypes.chunked(2).forEach { pair ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        pair.forEach { arch ->
                                            val isSel = editArchetype == arch
                                            Button(
                                                onClick = { editArchetype = arch },
                                                contentPadding = PaddingValues(horizontal = 4.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSel) Color.Cyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f)
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.weight(1f).height(24.dp).border(
                                                    1.dp, if (isSel) Color.Cyan else Color.Transparent, RoundedCornerShape(4.dp)
                                                )
                                            ) {
                                                Text(arch, fontSize = 7.sp, color = if (isSel) Color.Cyan else Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.updateJamunAgentConfig(editAgentName, editModelUrl, editArchetype)
                                    Toast.makeText(context, "Identity synced successfully!", Toast.LENGTH_SHORT).show()
                                    showConfigPanel = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            ) {
                                Text("APPLY & SAVE IDENTITY STATUS", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (!isJamunDownloaded && !isJamunDownloading) {
                    // State: Model Uninstalled
                    Text(
                        text = "$jamunAgentName lives bound to your device CPU context as an offline-first Neural Watcher. Granting full sandbox control to monitor processes 24/7, intercept thread errors, and plan storage alignments.",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hugging Face registry: $jamunAgentModelUrl",
                        fontSize = 8.sp,
                        color = Color.Cyan.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.startJamunDownload() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3000)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("jamun_download_btn")
                    ) {
                        Text("DOWNLOAD & ENGAGE ${jamunAgentName.uppercase()} AGENT GGUF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isJamunDownloading) {
                    // State: Model downloading
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val modelNameExt = jamunAgentModelUrl.substringAfterLast("/")
                        Text("RESOLVING $modelNameExt FORCE GGUF WEIGHTS...", fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text("${jamunDownloadProgress.toInt()}%", fontSize = 10.sp, color = Color.Cyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { jamunDownloadProgress / 100f },
                        color = Color(0xFFFF3D00),
                        trackColor = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                } else {
                    // State: Model Downloaded!
                    if (showJamunGreeting || !hasFirstGreetingBeenAllowed) {
                        // Futuristic Cyberpunk greetings panel!
                        val sysName = userProfile?.displayName ?: "User"
                        val modelNameExt = jamunAgentModelUrl.substringAfterLast("/")
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Cyan, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A29))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚡ CYBERPUNK HUD CUSTOM TRANSMISSION:",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Cyan,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Hello '$sysName' I am '$jamunAgentName' (${jamunAgentArchetype}), compiled dynamically from Hugging Face model '$modelNameExt'. If you allow me, I will audit your hardware core logs, regulate temperature limits, and secure cache traces.",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.triggerJamunAuditDecision(true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.15f), contentColor = Color.Green),
                                        modifier = Modifier.weight(1f).height(32.dp).border(1.dp, Color.Green, RoundedCornerShape(6.dp)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("ACCEPT AUDIT PLAN", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.triggerJamunAuditDecision(false) },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("BYPASS/STANDBY", fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    } else if (isJamunAuditing) {
                        // Monitoring Audit scans active!
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color.Cyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("ACTIVE THREAD WATCHER SYSTEM STATE DIAGNOSTICS ACTIVE...", fontSize = 9.sp, color = Color.Cyan, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        // Diagnostic wizard steps 1 to 5, or Step 6 (Complete)
                        when (jamunAuditStep) {
                            0 -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("Status: $jamunAgentName GGUF ONLINE & STANDBY", fontSize = 10.sp, color = Color.Green, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.restartJamunAudit() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan.copy(alpha = 0.15f), contentColor = Color.Cyan),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(34.dp).border(1.dp, Color.Cyan, RoundedCornerShape(6.dp))
                                    ) {
                                        Text("TRIGGER JETPACK SYSTEM AUDIT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            1 -> {
                                JamunStepView(
                                    stepNum = 1,
                                    title = "Cloud Snapshots Transfer Protocol",
                                    desc = "Upload diagnostic backlogs & local system state configs to your Google GMail Drive folder, clearing system cache tracks.",
                                    allowedState = jamunStep1Allowed,
                                    onApprove = { viewModel.approveJamunStep(1, it) }
                                )
                            }
                            2 -> {
                                JamunStepView(
                                    stepNum = 2,
                                    title = "Flush Same Redundant Duplicate Files",
                                    desc = "Found 12 redundant duplicate diagnostic text logs totaling 420.5 MB cache heap. Clear duplicates?",
                                    allowedState = jamunStep2Allowed,
                                    onApprove = { viewModel.approveJamunStep(2, it) }
                                )
                            }
                            3 -> {
                                JamunStepView(
                                    stepNum = 3,
                                    title = "Hardware Multi-Core Clock Calibration",
                                    desc = "Align Starved CPU core thread queues, clear binding unregister delays, and prevent InputDispatcher faults.",
                                    allowedState = jamunStep3Allowed,
                                    onApprove = { viewModel.approveJamunStep(3, it) }
                                )
                            }
                            4 -> {
                                JamunStepView(
                                    stepNum = 4,
                                    title = "Social Cache Purge & App Sleep Sweeper",
                                    desc = "Instantly sleep 8 non-vital background processes and purge 250 MB heavy temporary internet caches.",
                                    allowedState = jamunStep4Allowed,
                                    onApprove = { viewModel.approveJamunStep(4, it) }
                                )
                            }
                            5 -> {
                                JamunStepView(
                                    stepNum = 5,
                                    title = "Incorporate 24/7 Monitoring Daemon",
                                    desc = "Boot real-time secure daemon thread to isolate bad app layouts, auto force stop background runaways and turn off invasive permissions.",
                                    allowedState = jamunStep5Allowed,
                                    onApprove = { viewModel.approveJamunStep(5, it) }
                                )
                            }
                            6 -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("🟢 ALL SYSTEMS OPTIMISED BY ${jamunAgentName.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Green, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$jamunAgentName local thread watcher lives in device heap protecting memory boundaries. All confirmation steps cleared, ready to run again any time.", fontSize = 9.sp, color = Color.LightGray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.restartJamunAudit() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3000)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(34.dp)
                                        ) {
                                            Text("RE-AUDIT NOW", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isJamunDownloaded && !isJamunDownloading && jamunAuditStep == 0 && !showJamunGreeting && hasFirstGreetingBeenAllowed) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151821)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFFF5722).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚡ CONSOLE: $jamunAgentName'S ACTIVE AGENTIC POWERS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8A65),
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Power 1: CPU Overclocker
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("CPU Core High Overclock Boost", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Force clocks to maximum frequency parameters", fontSize = 8.sp, color = Color.Gray)
                                    }
                                    Switch(
                                        checked = isCpuOverclocked,
                                        onCheckedChange = { viewModel.setCpuOverclocked(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFFF5722),
                                            checkedTrackColor = Color(0xFFFF5722).copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.scale(0.7f).testTag("cpu_overclock_switch")
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(6.dp))

                                // Power 2: Thermal reduction
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Liquid Cooling Thermo Dissipation", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Chill cores to 23.5°C to protect silicon life", fontSize = 8.sp, color = Color.Gray)
                                    }
                                    Switch(
                                        checked = isThermalsCooled,
                                        onCheckedChange = { viewModel.setThermalsCooled(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Cyan,
                                            checkedTrackColor = Color.Cyan.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.scale(0.7f).testTag("thermals_cool_switch")
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(6.dp))

                                // Power 3: Frame Lag Resolver
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Compose Layout Lag Deflector", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Inject layout deflection buffers to sustain 120 FPS", fontSize = 8.sp, color = Color.Gray)
                                    }
                                    Switch(
                                        checked = isUiLagResolved,
                                        onCheckedChange = { viewModel.setUiLagResolved(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Green,
                                            checkedTrackColor = Color.Green.copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.scale(0.7f).testTag("ui_lag_resolved_switch")
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.deleteJamunDownloadedModel() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f), contentColor = Color.Red),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().height(26.dp).border(1.dp, Color.Red, RoundedCornerShape(6.dp)),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("PURGE & DE-REGISTER HF MODEL", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Active audit logs dashboard console
                if (jamunAuditLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Black)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(jamunAuditLogs.asReversed()) { log ->
                                Text(
                                    text = log,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (log.contains("[DOWNLOADER]")) Color(0xFFFF9100) else if (log.contains("ONLINE") || log.contains("RESOLVED")) Color.Green else Color.Cyan,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Collapsible Neural Learning Hub & Standalone Offline Small Model Controls ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { learningHubExpanded = !learningHubExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Optina Neural Learning & Offline Slabs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = activeTextColor)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Score: ${agentLearningScore}pts",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Icon(
                            imageVector = if (learningHubExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Collapse logo",
                            tint = activeTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = learningHubExpanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text("DYNAMICALLY LEARNED SCHEMAS (AOT SYSTEM):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        agentLearnedSpecs.takeLast(4).forEach { spec ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                                Text(spec, fontSize = 10.sp, color = activeTextColor)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Text("OFFLINE FALLBACK MODEL REGISTRY:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Small offline models picker list
                        offlineModelsList.forEach { model ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (model.name == selectedOfflineModel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                                    .clickable { viewModel.setSelectedOfflineModel(model.name) }
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Check, 
                                        contentDescription = "", 
                                        tint = if (model.name == selectedOfflineModel) MaterialTheme.colorScheme.primary else Color.Transparent, 
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(model.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = activeTextColor)
                                        Text("Config: ${model.parameterCount} | RAM footprint: ${model.sizeGB}", fontSize = 8.sp, color = activeSubTextColor)
                                    }
                                }
                                if (model.name == selectedOfflineModel) {
                                    Text("OFFLINE DEFAULT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { showAddModelDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Register Custom Offline Model", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 4. Scrollable chat messages pane ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                val isAI = message.sender == "assistant"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAI) Arrangement.Start else Arrangement.End
                ) {
                    if (isAI) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                )
                                .border(1.dp, activeBorderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "AI symbol",
                                tint = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    val bubbleBg = when {
                        isAI && isHighContrast -> Color.Black
                        isAI -> MaterialTheme.colorScheme.surfaceVariant
                        isHighContrast -> Color.White
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val bubbleBorder = if (isHighContrast) Color.White else Color.Transparent
                    val bubbleText = when {
                        isAI && isHighContrast -> Color.White
                        isAI -> MaterialTheme.colorScheme.onSurfaceVariant
                        isHighContrast -> Color.Black
                        else -> MaterialTheme.colorScheme.onPrimary
                    }

                    Card(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .border(1.dp, bubbleBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = bubbleBg),
                        shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isAI) 2.dp else 14.dp,
                            bottomEnd = if (isAI) 14.dp else 2.dp
                        )
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(12.dp),
                            color = bubbleText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    if (!isAI) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = CircleShape
                                )
                                .border(1.dp, activeBorderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "U",
                                fontWeight = FontWeight.Bold,
                                color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Spinner loader with multi-step explanation
            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sleek shimmer loading indicator
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            // Pulsing text effect
                            Text(
                                text = currentThinkingStep,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isBrowseActive) {
                                Text(
                                    text = "🔍 Browsing active index for live system techniques...",
                                    fontSize = 9.sp,
                                    color = activeSubTextColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Quick Touch Prompt suggestions ---
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Clean up residual cache junk [CLEAN_JUNK]" to "Clean System Cache",
                "Reduce system drain [BATTERY_SAVER]" to "Regulate Battery Saving",
                "Force hibernation on idling leakers" to "Freeze dynamic apps",
                "Run E2E backup snapshot of local logs" to "Save snapshot backup"
            ).forEach { (triggerText, chipsTitle) ->
                Box(
                    modifier = Modifier
                        .padding(vertical = 3.dp)
                        .background(
                            color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { textInputState = triggerText }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        chipsTitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeTextColor
                    )
                }
            }
        }

        // --- 6. Attachment Preview Bar ---
        if (attachedDocument != null || attachedImage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                attachedDocument?.let { doc ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = "", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(doc, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "De-attach",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { attachedDocument = null }
                            )
                        }
                    }
                }

                attachedImage?.let { img ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(img, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "De-attach",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { attachedImage = null }
                            )
                        }
                    }
                }
            }
        }

        // --- 7. Voice Recording Simulated Waveform overlay ---
        if (isRecordingVoice) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = "Mic Recording in progress", tint = Color.Red, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recording Voice: $voiceRecordTimer sec...", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Simulated pulsing Equalizer visual wave bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseHeight1 by infiniteTransition.animateFloat(
                        initialValue = 8f, targetValue = 28f,
                        animationSpec = infiniteRepeatable(animation = tween(400), repeatMode = RepeatMode.Reverse),
                        label = "p1"
                    )
                    val pulseHeight2 by infiniteTransition.animateFloat(
                        initialValue = 18f, targetValue = 10f,
                        animationSpec = infiniteRepeatable(animation = tween(300), repeatMode = RepeatMode.Reverse),
                        label = "p2"
                    )
                    val pulseHeight3 by infiniteTransition.animateFloat(
                        initialValue = 12f, targetValue = 32f,
                        animationSpec = infiniteRepeatable(animation = tween(500), repeatMode = RepeatMode.Reverse),
                        label = "p3"
                    )

                    Box(modifier = Modifier.width(3.dp).height(pulseHeight1.dp).background(Color.Red, RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.width(3.dp).height(pulseHeight2.dp).background(Color.Red, RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.width(3.dp).height(pulseHeight3.dp).background(Color.Red, RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.width(3.dp).height(pulseHeight1.dp).background(Color.Red, RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.width(3.dp).height(pulseHeight2.dp).background(Color.Red, RoundedCornerShape(2.dp)))
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { isRecordingVoice = false }
                ) {
                    Text("DONE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }

        // --- 8. Inputs Text Field actions layout (Bottom Bar sticky) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document Attachment Icon clip
            IconButton(
                onClick = {
                    attachedDocument = if (attachedDocument == null) "diagnostics_metrics_report.pdf" else null
                    if (attachedDocument != null) {
                        Toast.makeText(context, "Attached document diagnostics_metrics_report.pdf", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach PDF/Docs",
                    tint = if (attachedDocument != null) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Image Gallery Icon clip
            IconButton(
                onClick = {
                    attachedImage = if (attachedImage == null) "cache_dump_screenshot_#42.jpg" else null
                    if (attachedImage != null) {
                        Toast.makeText(context, "Attached screenshot from Gallery", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Attach Image",
                    tint = if (attachedImage != null) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Microphone Icon clip (Voice Chat stimulation)
            IconButton(
                onClick = {
                    if (!isAiAgentEnabled) {
                        Toast.makeText(context, "Enable Optina Agent to activate voice commands!", Toast.LENGTH_SHORT).show()
                        return@IconButton
                    }
                    scope.launch {
                        isRecordingVoice = true
                        voiceRecordTimer = 3
                        while (voiceRecordTimer > 0 && isRecordingVoice) {
                            delay(1000)
                            voiceRecordTimer--
                        }
                        if (isRecordingVoice) {
                            // Automatically transcribe simulated voice message
                            textInputState = "Please run deep optimization audit sweep under local weights now."
                            isRecordingVoice = false
                            Toast.makeText(context, "Voice transcripted successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Speech Chat",
                    tint = if (isRecordingVoice) Color.Red else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            OutlinedTextField(
                value = textInputState,
                onValueChange = { textInputState = it },
                placeholder = { 
                    Text(
                        if (!isAiAgentEnabled) "Optina Disabled. Enable to converse..." 
                        else "Ask Optina Agent to optimize device...", 
                        fontSize = 11.sp, 
                        color = activeSubTextColor
                    ) 
                },
                modifier = Modifier
                    .weight(1.0f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = activeTextColor,
                    unfocusedTextColor = activeTextColor,
                    focusedBorderColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textInputState.trim().isNotEmpty()) {
                        var payload = textInputState
                        if (isBrowseActive) {
                            payload = "$payload [WEB_BROWSE_ENABLED]"
                        }
                        if (attachedDocument != null) {
                            payload = "$payload (Attached document: $attachedDocument)"
                            attachedDocument = null
                        }
                        if (attachedImage != null) {
                            payload = "$payload (Attached photo: $attachedImage)"
                            attachedImage = null
                        }
                        
                        viewModel.sendChatMessage(payload)
                        textInputState = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Modal dialog to add small model manually
    if (showAddModelDialog) {
        AlertDialog(
            onDismissRequest = { showAddModelDialog = false },
            title = { Text("Register Small Offline Model", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add a custom model path to compile local weights inside the Optina storage registry.", fontSize = 11.sp, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = newModelName,
                        onValueChange = { newModelName = it },
                        label = { Text("Model Name (e.g. TonyLama v2)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newModelParams,
                        onValueChange = { newModelParams = it },
                        label = { Text("Parameter Size (e.g. 0.8 B)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newModelSize,
                        onValueChange = { newModelSize = it },
                        label = { Text("Memory Impact (e.g. 0.6 GB)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newModelName.trim().isNotEmpty()) {
                            viewModel.addCustomOfflineModel(newModelName, newModelSize, newModelParams)
                            showAddModelDialog = false
                            newModelName = ""
                            Toast.makeText(context, "Added custom compact model!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please set model name!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Register Model")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddModelDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Simple modifier helper function for scaling switch inline
fun Modifier.scale(scale: Float): Modifier = graphicsLayer(scaleX = scale, scaleY = scale)

@Composable
fun JamunStepView(
    stepNum: Int,
    title: String,
    desc: String,
    allowedState: Boolean?,
    onApprove: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PHASE 0$stepNum/05: $title",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB300),
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (allowedState == true) "GRANTED" else if (allowedState == false) "DENIED" else "AWAITING PERMISSIONS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (allowedState == true) Color.Green else if (allowedState == false) Color.Red else Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = desc,
            fontSize = 10.sp,
            color = Color.LightGray,
            lineHeight = 14.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onApprove(true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.15f), contentColor = Color.Green),
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .border(1.dp, Color.Green.copy(alpha = 0.7f), RoundedCornerShape(6.dp)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("ALLOW OPERATION", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { onApprove(false) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("DENY/SKIP STEP", fontSize = 9.sp)
            }
        }
    }
}
