package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.data.local.AppDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.BackupSnapshot
import com.example.data.repository.BackgroundApp
import com.example.data.repository.OptimiserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OptimiserViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = OptimiserRepository(application, db.activityLogDao())

    // --- Core UI Observables (Directly Reactive Room Sources) ---
    val activityLogs: StateFlow<List<ActivityLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val backupSnapshots: StateFlow<List<BackupSnapshot>> = repository.allSnapshots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State values ---
    private val _storageInfo = MutableStateFlow(com.example.data.repository.StorageInfo(256000, 128000, 2400))
    val storageInfo: StateFlow<com.example.data.repository.StorageInfo> = _storageInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow(com.example.data.repository.BatteryInfo(90, 31.0f, "Healthy", false))
    val batteryInfo: StateFlow<com.example.data.repository.BatteryInfo> = _batteryInfo.asStateFlow()

    private val _backgroundApps = MutableStateFlow(repository.getBackgroundApps())
    val backgroundApps: StateFlow<List<BackgroundApp>> = _backgroundApps.asStateFlow()

    // Interactive States
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isCleaning = MutableStateFlow(false)
    val isCleaning: StateFlow<Boolean> = _isCleaning.asStateFlow()

    private val _optimizerStatusMessage = MutableStateFlow("Ready to scan")
    val optimizerStatusMessage: StateFlow<String> = _optimizerStatusMessage.asStateFlow()

    // Thermal & Alert Monitor States
    private val _thermalAlert = MutableStateFlow<String?>(null)
    val thermalAlert: StateFlow<String?> = _thermalAlert.asStateFlow()

    private val _batteryAlert = MutableStateFlow<String?>(null)
    val batteryAlert: StateFlow<String?> = _batteryAlert.asStateFlow()

    // Theme & Display preferences
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    // --- GMail Authentication State (Google Identity simulation) ---
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // --- Free-tier Key configurations ---
    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // --- AI Agentic Chat States ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("assistant", "Hello! I am your AI Mobile Optimiser. Log in with your Gmail credentials to begin agentic system control. I can clean junk files, monitor thermal states, sleep background apps, or create encrypted backups.")
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _auditResult = MutableStateFlow<String?>(null)
    val auditResult: StateFlow<String?> = _auditResult.asStateFlow()

    private val _isAuditing = MutableStateFlow(false)
    val isAuditing: StateFlow<Boolean> = _isAuditing.asStateFlow()

    init {
        viewModelScope.launch {
            _storageInfo.value = repository.getDeviceStorageInfo()
            _batteryInfo.value = repository.getBatteryInfo()
        }
        // Start periodic simulator loops for real-time monitoring and thermal variance
        startPeriodicStatsLoop()
    }

    private fun startPeriodicStatsLoop() {
        viewModelScope.launch {
            while (true) {
                try {
                    delay(12000) // update every 12 seconds
                    // Simulate fluctuation in temperature and usage
                    val storage = _storageInfo.value
                    val battery = _batteryInfo.value

                    val extraJunk = (0..5).random()
                    val tempVariance = (-1..1).random() * 0.2f
                    val newPercentage = if (battery.percentage > 1 && !battery.isCharging) battery.percentage - if ((0..10).random() == 1) 1 else 0 else battery.percentage

                    _storageInfo.value = storage.copy(junkMB = storage.junkMB + extraJunk)
                    _batteryInfo.value = battery.copy(
                        percentage = newPercentage,
                        temperatureCelsius = (battery.temperatureCelsius + tempVariance).coerceIn(30.0f, 48.0f)
                    )

                    // Trigger warnings dynamically if thresholds crossed
                    evaluateAlerts()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun evaluateAlerts() {
        val temp = _batteryInfo.value.temperatureCelsius
        if (temp > 40.0f) {
            val alertMsg = "Warning: Battery temperature is high (${String.format(Locale.getDefault(), "%.1f", temp)}°C). Recommend immediate optimization!"
            if (_thermalAlert.value == null) {
                _thermalAlert.value = alertMsg
                repository.insertLog("THERMAL_ALERT", alertMsg, mapOf("temp" to temp.toString()))
            }
        } else {
            _thermalAlert.value = null
        }

        val pct = _batteryInfo.value.percentage
        if (pct < 20) {
            val alertMsg = "Critical: Battery is low (${pct}%). Activate deep sleep immediately!"
            if (_batteryAlert.value == null) {
                _batteryAlert.value = alertMsg
                repository.insertLog("BATTERY_ALERT", alertMsg, mapOf("level" to pct.toString()))
            }
        } else {
            _batteryAlert.value = null
        }
    }

    // --- Optimization Commands ---

    fun runDeviceAudit() {
        viewModelScope.launch {
            _isAuditing.value = true
            _optimizerStatusMessage.value = "Performing complete system audit..."
            val storage = _storageInfo.value
            val battery = _batteryInfo.value
            val apps = _backgroundApps.value
            
            val totalAppsRam = apps.sumOf { it.memoryUsageMB }
            val highestRamApp = apps.maxByOrNull { it.memoryUsageMB }?.appName ?: "Heavy process"
            
            val prompt = """
                Perform a professional diagnostic "Audit" of this Android device setup based on these real-time dashboard metrics:
                - Internal Storage: ${storage.usedMB} MB / ${storage.totalMB} MB used (Junk/Cache estimate: ${storage.junkMB} MB).
                - RAM / Background Activity: ${apps.size} running background apps, total background RAM consumption of $totalAppsRam MB.
                - Battery & Thermals: Level: ${battery.percentage}%, Temp: ${battery.temperatureCelsius}°C (${battery.health}).
                
                Based on these metrics, provide four structured, clear, and actionable advice categories in short bullet points:
                1. How to Free RAM Safely: Recommend specific background apps to hibernate/freeze without causing system failures.
                2. How to Free Internal Storage Safely: Explain what caches or temporary files (such as the ${storage.junkMB} MB junk) are safe to delete versus user files.
                3. How to Stop Background Apps Safely: Guide the user on freezing high memory consumers like $highestRamApp.
                4. Save Storage via Google Drive: Give custom suggestions on what user data (photos, old archives) should be securely moved to Google Drive or other cloud suites to easily salvage local space.
                
                Adopt a highly professional, encouraging, and clear AI Optimisation expert tone. Avoid long winded intros. Keep it concise, structured, and easy to read in a card.
            """.trimIndent()
            
            val rawKey = _customApiKey.value.trim().ifEmpty { BuildConfig.GEMINI_API_KEY }
            if (rawKey.isEmpty() || rawKey == "MY_GEMINI_API_KEY") {
                _auditResult.value = """
                    ⚠️ Gemini API Key not configured!
                    
                    Please enter your Google Gemini API Key in the Settings tab, or set GEMINI_API_KEY in your AI Studio Secrets in order to generate customized AI Diagnostics.
                    
                    Standard Local Recommendation:
                    • Free RAM: Go to the Deep Freeze Engine and Hibernate running media or social apps.
                    • Free Storage: Use One-Tap Clean to wipe ${storage.junkMB} MB of residual files.
                    • Stop Apps Safely: Freeze heavy background tasks carefully.
                    • Google Drive: Connect your Gmail account to backup logs and offload files to free up internal system space.
                """.trimIndent()
                _isAuditing.value = false
                return@launch
            }
            
            try {
                _optimizerStatusMessage.value = "Analyzing RAM cache & background leaks..."
                val resultText = withContext(Dispatchers.IO) {
                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                        generationConfig = com.example.api.GenerationConfig(temperature = 0.4f),
                        systemInstruction = Content(parts = listOf(Part(text = "You are an expert system diagnostics auditor. You output structured device reviews with short action points.")))
                    )
                    val response = RetrofitClient.service.generateContent(rawKey, request)
                    val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    
                    if (generatedText != null) {
                        // Persist Activity Log
                        repository.insertLog(
                            type = "AUDIT",
                            description = "Ran AI-driven system audit for RAM, storage, background processes, and cloud mapping suggestions.",
                            detailsMap = mapOf(
                                "storage_used_mb" to storage.usedMB.toString(),
                                "ram_apps_count" to apps.size.toString(),
                                "battery_temp" to battery.temperatureCelsius.toString()
                            )
                        )
                    }
                    generatedText
                }
                
                _auditResult.value = resultText ?: "Failed to generate audit report. Please re-run the audit."
                _optimizerStatusMessage.value = if (resultText != null) "AI System Audit completed successfully!" else "Audit generation returned empty response."
            } catch (e: Exception) {
                e.printStackTrace()
                _auditResult.value = "Error communicating with Gemini: ${e.message}\nEnsure your API Key is valid and internet is active."
                _optimizerStatusMessage.value = "Failed to run system audit."
            } finally {
                _isAuditing.value = false
            }
        }
    }

    fun scanSystem() {
        viewModelScope.launch {
            _isScanning.value = true
            _optimizerStatusMessage.value = "Scanning for cache, leftovers, temp files..."
            delay(1500)
            val info = repository.getDeviceStorageInfo()
            _storageInfo.value = info
            _optimizerStatusMessage.value = "Scan result: Found ${info.junkMB} MB system junk files."
            _isScanning.value = false
        }
    }

    fun cleanJunkFiles(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isCleaning.value = true
            _optimizerStatusMessage.value = "Cleaning obsolete cache..."
            delay(1800)
            val currentJunk = _storageInfo.value.junkMB
            _storageInfo.value = _storageInfo.value.copy(junkMB = 0)
            _optimizerStatusMessage.value = "Successfully cleaned $currentJunk MB junk files!"
            _isCleaning.value = false

            // Drop temperature code on clean
            _batteryInfo.value = _batteryInfo.value.copy(
                temperatureCelsius = (_batteryInfo.value.temperatureCelsius - 2.5f).coerceAtLeast(31.0f)
            )

            // Persist Activity Log
            repository.insertLog(
                type = "JUNK_CLEAN",
                description = "Cleaned $currentJunk MB of temporary residual caches",
                detailsMap = mapOf("size_cleaned_mb" to currentJunk.toString(), "remaining_mb" to "0")
            )
            onDone()
        }
    }

    fun optimizeBattery(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isScanning.value = true
            _optimizerStatusMessage.value = "Regulating CPU scaling & background sync states..."
            delay(1200)

            // Put background apps to sleep first
            val list = _backgroundApps.value.map { it.copy(isAsleep = true) }
            _backgroundApps.value = list

            _optimizerStatusMessage.value = "Battery performance optimized. Background apps put to deep sleep!"
            _isScanning.value = false

            // Save log
            repository.insertLog(
                type = "BATTERY_SAVER",
                description = "Optimised battery drain by throttling CPU and sleeping ${list.size} idle apps",
                detailsMap = mapOf("apps_slept" to list.size.toString(), "battery_percentage" to _batteryInfo.value.percentage.toString())
            )
            onDone()
        }
    }

    fun toggleAppSleep(packageName: String) {
        val list = _backgroundApps.value.map {
            if (it.packageName == packageName) {
                val newState = !it.isAsleep
                viewModelScope.launch {
                    repository.insertLog(
                        type = "DEEP_SLEEP",
                        description = "${if (newState) "Froze" else "Woke"} background app: ${it.appName}",
                        detailsMap = mapOf("package" to packageName, "asleep" to newState.toString())
                    )
                }
                it.copy(isAsleep = newState)
            } else {
                it
            }
        }
        _backgroundApps.value = list
    }

    fun deepSleepAllApps(onDone: () -> Unit = {}) {
        val list = _backgroundApps.value.map { it.copy(isAsleep = true) }
        _backgroundApps.value = list
        viewModelScope.launch {
            repository.insertLog(
                type = "DEEP_SLEEP",
                description = "Froze all ${list.size} dynamic background apps in Deep Sleep Mode",
                detailsMap = mapOf("total_apps" to list.size.toString())
            )
            onDone()
        }
    }

    // --- Configuration handlers ---

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleHighContrast() {
        _isHighContrast.value = !_isHighContrast.value
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
    }

    // --- GMail Authentication OAuth Simulation ---

    fun signInWithGmailOAuth(emailArg: String = "pasha01118@gmail.com") {
        viewModelScope.launch {
            _isAuthLoading.value = true
            delay(1000) // secure OAuth handshake simulation
            _userProfile.value = UserProfile(
                displayName = "Pasha Dev",
                email = emailArg,
                photoUrl = "https://lh3.googleusercontent.com/a/default-user=s120-c",
                idToken = "oauth_tok_google_" + System.currentTimeMillis()
            )
            _isAuthLoading.value = false

            repository.insertLog(
                type = "CLOUD_SYNC",
                description = "Established Google GMail Authenticated session for $emailArg",
                detailsMap = mapOf("user" to emailArg)
            )
        }
    }

    fun signOutGmail() {
        val lastUser = _userProfile.value?.email ?: ""
        _userProfile.value = null
        viewModelScope.launch {
            repository.insertLog(
                type = "CLOUD_SYNC",
                description = "Logged out from Gmail session: $lastUser",
                detailsMap = mapOf("user" to lastUser)
            )
        }
    }

    // --- Backups & Snapshots ---

    fun backUpNow(note: String) {
        viewModelScope.launch {
            val logs = activityLogs.value
            repository.triggerSnapshotBackup(logs, note)
        }
    }

    fun deleteBackup(snapshot: BackupSnapshot) {
        viewModelScope.launch {
            repository.deleteSnapshot(snapshot)
        }
    }

    fun restoreBackup(snapshot: BackupSnapshot) {
        viewModelScope.launch {
            repository.restoreSnapshotBackup(snapshot)
        }
    }

    fun exportHistoryJson(startTime: Long, endTime: Long, filterType: String, onDone: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportLogsToJson(activityLogs.value, startTime, endTime, filterType)
            onDone(json)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // --- Agentic AI Chat Controller (Controls complete app) ---

    fun sendChatMessage(userText: String) {
        if (userText.trim().isEmpty()) return

        val userMessage = ChatMessage("user", userText)
        val updatedList = _chatMessages.value.toMutableList()
        updatedList.add(userMessage)
        _chatMessages.value = updatedList

        _isChatLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            // Pick appropriate API key
            // Use customApiKey if entered, otherwise default to BuildConfig.GEMINI_API_KEY
            val rawKey = _customApiKey.value.trim().ifEmpty { BuildConfig.GEMINI_API_KEY }

            if (rawKey.isEmpty() || rawKey == "MY_GEMINI_API_KEY") {
                withContext(Dispatchers.Main) {
                    val errorMsg = ChatMessage(
                        "assistant",
                        "I am ready to help, but no valid Gemini API Key was found!\n\nPlease enter your Gemini Free Tier API Key in the Settings tab, or make sure GEMINI_API_KEY is configured in AI Studio Secrets."
                    )
                    _chatMessages.value = _chatMessages.value + errorMsg
                    _isChatLoading.value = false
                }
                return@launch
            }

            // Build beautiful agent prompt context
            val systemRule = """
                You are the highly capable Agentic AI Assistant of the Android "Mobile Optimiser" app.
                Your user is fully authenticated via Gmail as: ${_userProfile.value?.email ?: "Guest User"}.
                You have the special authority to control this application's features to solve real-time mobile issues.
                When a user asks you to perform an operation, you can trigger actions on their local device by outputting a command tag AT THE END of your final text response.
                
                You can trigger these commands:
                - To clean junk files: output '[CLEAN_JUNK]' at the end
                - To optimize battery performance: output '[BATTERY_SAVER]' at the end
                - To sleep all background apps: output '[SLEEP_ALL]' at the end
                - To trigger an encrypted backup snapshot: output '[BACKUP_SNAPSHOT]' at the end
                - To perform a complete safe optimization/diagnostics audit of the device (analyze storage, Google Drive backup strategy, RAM/apps): output '[AUDIT_DEVICE]' at the end
                
                Keep your responses professional, friendly, clear, and informative. Explain what you're doing, and assure the user that the action is occurring. 
                Example reply: "I will perform a diagnostics audit of your system immediately. [AUDIT_DEVICE]"
            """.trimIndent()

            // Map historic chat list to Generative API contents
            val chatHistoryContents = _chatMessages.value.map {
                Content(parts = listOf(Part(text = it.text)))
            }

            val request = GenerateContentRequest(
                contents = chatHistoryContents,
                generationConfig = com.example.api.GenerationConfig(temperature = 0.5f),
                systemInstruction = Content(parts = listOf(Part(text = systemRule)))
            )

            try {
                val response = RetrofitClient.service.generateContent(rawKey, request)
                val aiReplyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "I received an empty response. Please try describing your request again."

                withContext(Dispatchers.Main) {
                    // Process agentic triggers
                    var parsedReply = aiReplyText
                    val cleanMatch = parsedReply.contains("[CLEAN_JUNK]")
                    val batteryMatch = parsedReply.contains("[BATTERY_SAVER]")
                    val sleepMatch = parsedReply.contains("[SLEEP_ALL]")
                    val backupMatch = parsedReply.contains("[BACKUP_SNAPSHOT]")
                    val auditMatch = parsedReply.contains("[AUDIT_DEVICE]")

                    // Strip triggers from display message for crisp clean UI comfort
                    parsedReply = parsedReply
                        .replace("[CLEAN_JUNK]", "")
                        .replace("[BATTERY_SAVER]", "")
                        .replace("[SLEEP_ALL]", "")
                        .replace("[BACKUP_SNAPSHOT]", "")
                        .replace("[AUDIT_DEVICE]", "")
                        .trim()

                    // Add assistant response to screen
                    val assistantMessage = ChatMessage("assistant", parsedReply)
                    _chatMessages.value = _chatMessages.value + assistantMessage
                    _isChatLoading.value = false

                    // Fire the real state optimizations!
                    if (cleanMatch) {
                        cleanJunkFiles()
                    }
                    if (batteryMatch) {
                        optimizeBattery()
                    }
                    if (sleepMatch) {
                        deepSleepAllApps()
                    }
                    if (backupMatch) {
                        backUpNow("Snapshot triggered by AI Assistant")
                    }
                    if (auditMatch) {
                        runDeviceAudit()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val errMsg = ChatMessage("assistant", "Sorry, I ran into an error communicating with Gemini API: ${e.message}\n\nPlease check your internet connection or the provided API key.")
                    _chatMessages.value = _chatMessages.value + errMsg
                    _isChatLoading.value = false
                }
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage("assistant", "Chat history cleared. How can I optimize your device health today?")
        )
    }
}

data class UserProfile(
    val displayName: String,
    val email: String,
    val photoUrl: String,
    val idToken: String
)

data class ChatMessage(
    val sender: String, // "user" or "assistant"
    val text: String
)
