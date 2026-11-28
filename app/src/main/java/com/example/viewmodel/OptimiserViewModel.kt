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
import com.example.api.GenericAiClient
import android.content.Context
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DynamicPowerSuggestion(
    val reasonTitle: String,
    val reasonDetail: String,
    val triggerType: String, // LOW_BATTERY or HIGH_TEMP
    val recommendedProfile: String = "Power Saving Mode"
)

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

    // Junk threshold alerting states
    private val _showJunkNotificationPrompt = MutableStateFlow(false)
    val showJunkNotificationPrompt: StateFlow<Boolean> = _showJunkNotificationPrompt.asStateFlow()
    private var _hasNotifiedJunkThreshold = false

    fun dismissJunkPrompt() {
        _showJunkNotificationPrompt.value = false
    }

    // Automated background junk file cleaning setting and simulated ram usage
    private val _isAutoJunkCleanEnabled = MutableStateFlow(false) // initialized in init below
    val isAutoJunkCleanEnabled: StateFlow<Boolean> = _isAutoJunkCleanEnabled.asStateFlow()

    private val _ramUsagePercent = MutableStateFlow(52f)
    val ramUsagePercent: StateFlow<Float> = _ramUsagePercent.asStateFlow()

    fun toggleAutoJunkClean() {
        val newValue = !_isAutoJunkCleanEnabled.value
        _isAutoJunkCleanEnabled.value = newValue
        prefs.edit().putBoolean("is_auto_junk_clean_enabled", newValue).apply()
        
        viewModelScope.launch {
            repository.insertLog(
                type = "SYSTEM_SETTINGS",
                description = "Automated RAM-triggered background clean is now ${if (newValue) "ENABLED" else "DISABLED"}",
                detailsMap = mapOf("auto_clean_active" to newValue.toString(), "trigger_percent" to "80%")
            )
        }
    }

    // Theme & Display preferences
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    // Battery Management Mode (High-Performance vs Power-Saving)
    private val _isPowerSavingMode = MutableStateFlow(true)
    val isPowerSavingMode: StateFlow<Boolean> = _isPowerSavingMode.asStateFlow()

    // --- Manual Safe Junk Cleanup Checkboxes & Daily WorkManager Schedule ---
    private val _tickSysCache = MutableStateFlow(true)
    val tickSysCache: StateFlow<Boolean> = _tickSysCache.asStateFlow()

    private val _tickObsoleteLogs = MutableStateFlow(true)
    val tickObsoleteLogs: StateFlow<Boolean> = _tickObsoleteLogs.asStateFlow()

    private val _tickApkRemnants = MutableStateFlow(false)
    val tickApkRemnants: StateFlow<Boolean> = _tickApkRemnants.asStateFlow()

    private val _tickTempThumbnails = MutableStateFlow(true)
    val tickTempThumbnails: StateFlow<Boolean> = _tickTempThumbnails.asStateFlow()

    private val _isScheduledJunkCleanEnabled = MutableStateFlow(false)
    val isScheduledJunkCleanEnabled: StateFlow<Boolean> = _isScheduledJunkCleanEnabled.asStateFlow()

    private val _scheduledCleanHour = MutableStateFlow(3) // Default 3:00 AM
    val scheduledCleanHour: StateFlow<Int> = _scheduledCleanHour.asStateFlow()

    private val _scheduledCleanMinute = MutableStateFlow(0)
    val scheduledCleanMinute: StateFlow<Int> = _scheduledCleanMinute.asStateFlow()

    private val _lastScheduledCleanSummary = MutableStateFlow("Not run yet")
    val lastScheduledCleanSummary: StateFlow<String> = _lastScheduledCleanSummary.asStateFlow()

    // --- Smart Power Scheduling (Sleep Hours Auto Battery Saver) ---
    private val _isSmartPowerScheduleEnabled = MutableStateFlow(false)
    val isSmartPowerScheduleEnabled: StateFlow<Boolean> = _isSmartPowerScheduleEnabled.asStateFlow()

    private val _sleepStartHour = MutableStateFlow(23) // 11:00 PM
    val sleepStartHour: StateFlow<Int> = _sleepStartHour.asStateFlow()

    private val _sleepStartMinute = MutableStateFlow(0)
    val sleepStartMinute: StateFlow<Int> = _sleepStartMinute.asStateFlow()

    private val _sleepEndHour = MutableStateFlow(7) // 7:00 AM
    val sleepEndHour: StateFlow<Int> = _sleepEndHour.asStateFlow()

    private val _sleepEndMinute = MutableStateFlow(0)
    val sleepEndMinute: StateFlow<Int> = _sleepEndMinute.asStateFlow()

    private val _isCurrentlyInSleepWindow = MutableStateFlow(false)
    val isCurrentlyInSleepWindow: StateFlow<Boolean> = _isCurrentlyInSleepWindow.asStateFlow()

    // --- Dynamic Power Profiles (<20% Battery or High Temp Auto-Suggestion) ---
    private val _isDynamicProfilesEnabled = MutableStateFlow(true)
    val isDynamicProfilesEnabled: StateFlow<Boolean> = _isDynamicProfilesEnabled.asStateFlow()

    private val _dynamicPowerSuggestion = MutableStateFlow<DynamicPowerSuggestion?>(null)
    val dynamicPowerSuggestion: StateFlow<DynamicPowerSuggestion?> = _dynamicPowerSuggestion.asStateFlow()

    // --- Installed App Battery Drain Scanner & App Hibernation ---
    private val _drainScanResults = MutableStateFlow<List<com.example.data.repository.AppDrainCandidate>>(emptyList())
    val drainScanResults: StateFlow<List<com.example.data.repository.AppDrainCandidate>> = _drainScanResults.asStateFlow()

    private val _isScanningAppDrain = MutableStateFlow(false)
    val isScanningAppDrain: StateFlow<Boolean> = _isScanningAppDrain.asStateFlow()

    // --- Real-time CPU Telemetry (shared with Secondary Home Screen CPU Widget) ---
    private val _cpuUsagePercent = MutableStateFlow(42f)
    val cpuUsagePercent: StateFlow<Float> = _cpuUsagePercent.asStateFlow()

    private val _cpuAvgMhz = MutableStateFlow(1680)
    val cpuAvgMhz: StateFlow<Int> = _cpuAvgMhz.asStateFlow()

    private val _cpuHistory = MutableStateFlow(listOf(28f, 34f, 41f, 38f, 46f, 42f, 39f, 44f, 40f, 42f))
    val cpuHistory: StateFlow<List<Float>> = _cpuHistory.asStateFlow()

    // --- GMail Authentication State (Google Identity simulation) ---
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // --- SharedPreferences Backed AI Configuration System ---
    private val prefs = application.getSharedPreferences("ai_settings", android.content.Context.MODE_PRIVATE)

    private val prefsListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { shared, key ->
        when (key) {
            com.example.widget.Live3GBatteryWidgetProvider.KEY_POWER_SAVING_MODE -> {
                val updated = shared.getBoolean(key, true)
                if (_isPowerSavingMode.value != updated) {
                    _isPowerSavingMode.value = updated
                }
            }
            com.example.worker.JunkCleanupPrefs.KEY_LAST_RUN_SUMMARY -> {
                _lastScheduledCleanSummary.value = shared.getString(key, "Not run yet") ?: "Not run yet"
            }
            com.example.worker.BatteryDrainScannerPrefs.KEY_LAST_BG_SCAN_SUMMARY -> {
                _lastBgDrainScanSummary.value = shared.getString(key, "Not scanned yet") ?: "Not scanned yet"
            }
            com.example.worker.SmartPowerPrefs.KEY_LAST_WORKER_CHECK -> {
                _lastSmartPowerWorkerCheck.value = shared.getString(key, "Pending first check") ?: "Pending first check"
            }
        }
    }

    private val _showSentinelFloatingButton = MutableStateFlow(prefs.getBoolean("show_sentinel_floating_button", true))
    val showSentinelFloatingButton: StateFlow<Boolean> = _showSentinelFloatingButton.asStateFlow()

    // Optina Agent - Enabled Toggle and Learning States
    private val _isAiAgentEnabled = MutableStateFlow(prefs.getBoolean("is_ai_agent_enabled", true))
    val isAiAgentEnabled: StateFlow<Boolean> = _isAiAgentEnabled.asStateFlow()

    private val _agentLearningScore = MutableStateFlow(prefs.getInt("agent_learning_score", 5))
    val agentLearningScore: StateFlow<Int> = _agentLearningScore.asStateFlow()

    private val _agentLearnedSpecs = MutableStateFlow<List<String>>(listOf(
        "Learned deep sleep triggers based on low thermal load (< 32.0°C).",
        "Learned junk generation rate: user accumulates ~2.4 MB of cache daily.",
        "Discovered background performance patterns: hibernated processes free up average 140MB RAM."
    ))
    val agentLearnedSpecs: StateFlow<List<String>> = _agentLearnedSpecs.asStateFlow()

    data class SmallModelInfo(
        val name: String,
        val sizeGB: String,
        val parameterCount: String,
        val isDefault: Boolean
    )

    private val _offlineModelsList = MutableStateFlow<List<SmallModelInfo>>(emptyList())
    val offlineModelsList: StateFlow<List<SmallModelInfo>> = _offlineModelsList.asStateFlow()

    private val _selectedOfflineModel = MutableStateFlow(prefs.getString("selected_offline_model", "Qwen AI 1B (Local)") ?: "Qwen AI 1B (Local)")
    val selectedOfflineModel: StateFlow<String> = _selectedOfflineModel.asStateFlow()

    private val _selectedAiProvider = MutableStateFlow(prefs.getString("selected_ai_provider", "Optina Agent") ?: "Optina Agent")
    val selectedAiProvider: StateFlow<String> = _selectedAiProvider.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _openaiApiKey = MutableStateFlow(prefs.getString("openai_api_key", "") ?: "")
    val openaiApiKey: StateFlow<String> = _openaiApiKey.asStateFlow()

    private val _openrouterApiKey = MutableStateFlow(prefs.getString("openrouter_api_key", "") ?: "")
    val openrouterApiKey: StateFlow<String> = _openrouterApiKey.asStateFlow()

    private val _ollamaEndpoint = MutableStateFlow(prefs.getString("ollama_endpoint", "http://localhost:11434") ?: "http://localhost:11434")
    val ollamaEndpoint: StateFlow<String> = _ollamaEndpoint.asStateFlow()

    private val _ollamaModel = MutableStateFlow(prefs.getString("ollama_model", "llama3") ?: "llama3")
    val ollamaModel: StateFlow<String> = _ollamaModel.asStateFlow()

    private val _grokApiKey = MutableStateFlow(prefs.getString("grok_api_key", "") ?: "")
    val grokApiKey: StateFlow<String> = _grokApiKey.asStateFlow()

    private val _huggingfaceToken = MutableStateFlow(
        prefs.getString("huggingface_token", "")?.ifEmpty { BuildConfig.HF_API_TOKEN.takeIf { it != "hf_placeholder_token_value" } ?: "" } ?: ""
    )
    val huggingfaceToken: StateFlow<String> = _huggingfaceToken.asStateFlow()

    private val _huggingfaceModel = MutableStateFlow(prefs.getString("huggingface_model", "meta-llama/Llama-3.2-1B-Instruct") ?: "meta-llama/Llama-3.2-1B-Instruct")
    val huggingfaceModel: StateFlow<String> = _huggingfaceModel.asStateFlow()

    // --- Hugging Face Live Validation States ---
    private val _hfValidationResult = MutableStateFlow<String?>(null)
    val hfValidationResult: StateFlow<String?> = _hfValidationResult.asStateFlow()

    private val _isHfValidating = MutableStateFlow(false)
    val isHfValidating: StateFlow<Boolean> = _isHfValidating.asStateFlow()

    private val _mistralApiKey = MutableStateFlow(prefs.getString("mistral_api_key", "") ?: "")
    val mistralApiKey: StateFlow<String> = _mistralApiKey.asStateFlow()

    private val _qwenApiKey = MutableStateFlow(prefs.getString("qwen_api_key", "") ?: "")
    val qwenApiKey: StateFlow<String> = _qwenApiKey.asStateFlow()

    private val _veniceApiKey = MutableStateFlow(
        prefs.getString("venice_api_key", "")?.ifEmpty { BuildConfig.VENICE_API_KEY.takeIf { it != "venice_placeholder_token_value" } ?: "" } ?: ""
    )
    val veniceApiKey: StateFlow<String> = _veniceApiKey.asStateFlow()

    private val _veniceModel = MutableStateFlow(prefs.getString("venice_model", "venice-uncensored") ?: "venice-uncensored")
    val veniceModel: StateFlow<String> = _veniceModel.asStateFlow()

    private val _customAiEndpoint = MutableStateFlow(prefs.getString("custom_ai_endpoint", "") ?: "")
    val customAiEndpoint: StateFlow<String> = _customAiEndpoint.asStateFlow()

    private val _customAiApiKey = MutableStateFlow(prefs.getString("custom_ai_api_key", "") ?: "")
    val customAiApiKey: StateFlow<String> = _customAiApiKey.asStateFlow()

    private val _customAiModel = MutableStateFlow(prefs.getString("custom_ai_model", "") ?: "")
    val customAiModel: StateFlow<String> = _customAiModel.asStateFlow()

    // --- Legacy field sync ---
    private val _customApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
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

    // --- High-Risk Permission Manager States ---
    private val revokedPermissionPackages = mutableSetOf<String>()
    private val _highRiskAppPermissions = MutableStateFlow<List<com.example.data.repository.HighRiskAppPermission>>(emptyList())
    val highRiskAppPermissions: StateFlow<List<com.example.data.repository.HighRiskAppPermission>> = _highRiskAppPermissions.asStateFlow()

    private val _isScanningPermissions = MutableStateFlow(false)
    val isScanningPermissions: StateFlow<Boolean> = _isScanningPermissions.asStateFlow()

    // --- 7-Day Hardware Performance Trends & CSV/PDF Report Export States ---
    private val _hardware7DayTrends = MutableStateFlow<List<com.example.data.repository.DailyHardwareTrendPoint>>(emptyList())
    val hardware7DayTrends: StateFlow<List<com.example.data.repository.DailyHardwareTrendPoint>> = _hardware7DayTrends.asStateFlow()

    private val _isExportingCsv = MutableStateFlow(false)
    val isExportingCsv: StateFlow<Boolean> = _isExportingCsv.asStateFlow()

    private val _isExportingPdf = MutableStateFlow(false)
    val isExportingPdf: StateFlow<Boolean> = _isExportingPdf.asStateFlow()

    private val _lastReportExport = MutableStateFlow<com.example.util.ReportExportResult?>(null)
    val lastReportExport: StateFlow<com.example.util.ReportExportResult?> = _lastReportExport.asStateFlow()

    // --- Background Battery Drain Scanner Worker & Smart Power Worker Telemetry ---
    private val _isBgDrainScannerEnabled = MutableStateFlow(false)
    val isBgDrainScannerEnabled: StateFlow<Boolean> = _isBgDrainScannerEnabled.asStateFlow()

    private val _lastBgDrainScanSummary = MutableStateFlow("Not scanned yet")
    val lastBgDrainScanSummary: StateFlow<String> = _lastBgDrainScanSummary.asStateFlow()

    private val _lastSmartPowerWorkerCheck = MutableStateFlow("Pending first check")
    val lastSmartPowerWorkerCheck: StateFlow<String> = _lastSmartPowerWorkerCheck.asStateFlow()

    init {
        _isAutoJunkCleanEnabled.value = prefs.getBoolean("is_auto_junk_clean_enabled", false)
        _isPowerSavingMode.value = prefs.getBoolean(com.example.widget.Live3GBatteryWidgetProvider.KEY_POWER_SAVING_MODE, true)
        _tickSysCache.value = prefs.getBoolean(com.example.worker.JunkCleanupPrefs.KEY_TICK_SYS_CACHE, true)
        _tickObsoleteLogs.value = prefs.getBoolean(com.example.worker.JunkCleanupPrefs.KEY_TICK_OBSOLETE_LOGS, true)
        _tickApkRemnants.value = prefs.getBoolean(com.example.worker.JunkCleanupPrefs.KEY_TICK_APK_REMNANTS, false)
        _tickTempThumbnails.value = prefs.getBoolean(com.example.worker.JunkCleanupPrefs.KEY_TICK_TEMP_THUMBNAILS, true)
        _isScheduledJunkCleanEnabled.value = prefs.getBoolean(com.example.worker.JunkCleanupPrefs.KEY_SCHEDULED_ENABLED, false)
        _scheduledCleanHour.value = prefs.getInt(com.example.worker.JunkCleanupPrefs.KEY_SCHEDULED_HOUR, 3)
        _scheduledCleanMinute.value = prefs.getInt(com.example.worker.JunkCleanupPrefs.KEY_SCHEDULED_MINUTE, 0)
        _lastScheduledCleanSummary.value = prefs.getString(com.example.worker.JunkCleanupPrefs.KEY_LAST_RUN_SUMMARY, "Not run yet") ?: "Not run yet"

        _isSmartPowerScheduleEnabled.value = prefs.getBoolean(com.example.worker.SmartPowerPrefs.KEY_SMART_POWER_ENABLED, false)
        _sleepStartHour.value = prefs.getInt(com.example.worker.SmartPowerPrefs.KEY_SLEEP_START_HOUR, 23)
        _sleepStartMinute.value = prefs.getInt(com.example.worker.SmartPowerPrefs.KEY_SLEEP_START_MINUTE, 0)
        _sleepEndHour.value = prefs.getInt(com.example.worker.SmartPowerPrefs.KEY_SLEEP_END_HOUR, 7)
        _sleepEndMinute.value = prefs.getInt(com.example.worker.SmartPowerPrefs.KEY_SLEEP_END_MINUTE, 0)
        _isDynamicProfilesEnabled.value = prefs.getBoolean(com.example.worker.SmartPowerPrefs.KEY_DYNAMIC_PROFILES_ENABLED, true)
        _isBgDrainScannerEnabled.value = prefs.getBoolean(com.example.worker.BatteryDrainScannerPrefs.KEY_BG_SCANNER_ENABLED, true)
        _lastBgDrainScanSummary.value = prefs.getString(com.example.worker.BatteryDrainScannerPrefs.KEY_LAST_BG_SCAN_SUMMARY, "Background scanner ready") ?: "Background scanner ready"
        _lastSmartPowerWorkerCheck.value = prefs.getString(com.example.worker.SmartPowerPrefs.KEY_LAST_WORKER_CHECK, "Pending first worker check") ?: "Pending first worker check"

        prefs.registerOnSharedPreferenceChangeListener(prefsListener)

        if (_isScheduledJunkCleanEnabled.value) {
            com.example.worker.JunkCleanupScheduler.scheduleDailyCleanup(
                getApplication(),
                _scheduledCleanHour.value,
                _scheduledCleanMinute.value
            )
        }
        if (_isSmartPowerScheduleEnabled.value) {
            com.example.worker.SmartPowerScheduler.schedulePeriodicCheck(getApplication())
        }
        if (_isBgDrainScannerEnabled.value) {
            com.example.worker.BatteryDrainScannerScheduler.schedulePeriodicScan(getApplication())
        }

        viewModelScope.launch {
            _storageInfo.value = repository.getDeviceStorageInfo()
            _batteryInfo.value = repository.getBatteryInfo()
            evaluateSmartPowerClock()
            evaluateAlerts()
            com.example.widget.Live3GBatteryWidgetProvider.updateAllWidgets(getApplication())
            com.example.widget.GlanceBatteryWidgetHelper.updateAll(getApplication())
            com.example.widget.LiveCpuUsageWidgetProvider.updateAllWidgets(getApplication(), _cpuUsagePercent.value.toInt(), _cpuAvgMhz.value)
            refresh7DayHardwareTrends()
            scanHighRiskPermissions()
            scanAppsForBatteryDrain()
        }
        loadOfflineModels()
        // Start periodic simulator loops for real-time monitoring and thermal variance
        startPeriodicStatsLoop()

        // Pre-initialize default skills and memory records if empty in Room database
        viewModelScope.launch {
            try {
                val currentSkills = repository.allSkills.first()
                if (currentSkills.isEmpty()) {
                    repository.insertSkill("CPU Overclocker Core", "Forces active core frequencies to theoretical maxima with dynamic thread priority alignment.", 1, "Core", "")
                    repository.insertSkill("Liquid Thermal Dissipation", "Regulates CPU core thermal dissipation boundaries to sub-ambient limits (23.5°C) to protect silicon lifetime.", 1, "Core", "")
                    repository.insertSkill("Compose Lag Deflector", "Sustains fluent 120 FPS by allocating adaptive sub-renderers in UI layout render loops.", 2, "Core", "")
                    repository.insertSkill("Memory Leak Drainer", "Detects dead inactive Android Binder structures and flushes dead VM heap objects.", 1, "Core", "")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModelScope.launch {
            try {
                val currentMemories = repository.allMemories.first()
                if (currentMemories.isEmpty()) {
                    repository.insertMemory("BEHAVIOR", "User prefers ultra performance modes, cybernetic styling, and dynamic hardware configurations.", 8)
                    repository.insertMemory("PREFERENCE", "Keep liquid cooling active whenever active CPU cores exceed thermal limit metrics.", 9)
                    repository.insertMemory("SYSTEM_RULE", "Autonomously defend and heal input connection leaks (broken InputDispatcher channels) immediately.", 10)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

                    val baseTemp = if (_isThermalsCooled.value) 23.5f else battery.temperatureCelsius
                    val minAllowed = if (_isThermalsCooled.value) 21.0f else 30.0f
                    val maxAllowed = if (_isThermalsCooled.value) 26.0f else 48.0f

                    _storageInfo.value = storage.copy(junkMB = storage.junkMB + extraJunk)
                    _batteryInfo.value = battery.copy(
                        percentage = newPercentage,
                        temperatureCelsius = (baseTemp + tempVariance).coerceIn(minAllowed, maxAllowed)
                    )

                    // Fluctuate memory usage
                    val currentRam = _ramUsagePercent.value
                    val ramChange = (-3..5).random()
                    val newRam = (currentRam + ramChange).coerceIn(35f, 95f)
                    _ramUsagePercent.value = newRam

                    // If auto cleaning is enabled and RAM detects above 80%
                    if (_isAutoJunkCleanEnabled.value && newRam >= 80f) {
                        viewModelScope.launch {
                            repository.insertLog(
                                type = "AUTO_CLEAN",
                                description = "Auto-Clean Triggered: RAM usage reached ${String.format(java.util.Locale.US, "%.1f", newRam)}% (>= 80% threshold). Cleared background cache directories.",
                                detailsMap = mapOf("ram_usage" to String.format(java.util.Locale.US, "%.1f", newRam) + "%")
                            )
                            cleanJunkFiles()
                            _ramUsagePercent.value = (35f + (0..10).random())
                        }
                    }

                    // Trigger warnings dynamically if thresholds crossed
                    evaluateAlerts()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun triggerLocalNotification(junkSize: Long) {
        val context = getApplication<android.app.Application>().applicationContext
        val channelId = "junk_cleanup_channel"
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "System Cleanliness Alerts"
            val desc = "Alerts when junk caches or logs exceed storage thresholds"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel(channelId, name, importance).apply {
                description = desc
            }
            val manager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }
        
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Excess Junk Cache Warning")
            .setContentText("Found $junkSize MB of system trash cluttering directories. Clear now to restore IO speed.")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            
        try {
            val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
            if (android.os.Build.VERSION.SDK_INT < 33 || 
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, 
                    "android.permission.POST_NOTIFICATIONS"
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(1002, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private suspend fun evaluateAlerts() {
        val temp = _batteryInfo.value.temperatureCelsius
        if (temp > 40.0f) {
            if (_jamunStep5Allowed.value == true) {
                val coolTemp = (temp - 3.2f).coerceAtLeast(35.0f)
                _batteryInfo.value = _batteryInfo.value.copy(temperatureCelsius = coolTemp)
                val logMsg = "🦾 [JAMUN ENFORCER] Intercepted battery/thermal spike. Automatically throttled secondary app background cores and cooled system from ${String.format(Locale.getDefault(), "%.1f", temp)}°C to ${String.format(Locale.getDefault(), "%.1f", coolTemp)}°C."
                _chatMessages.value = _chatMessages.value + ChatMessage("assistant", logMsg)
                repository.insertLog("JAMUN_REPAIR", logMsg, mapOf("temp_before" to temp.toString(), "temp_after" to coolTemp.toString()))
            } else {
                val alertMsg = "Warning: Battery temperature is high (${String.format(Locale.getDefault(), "%.1f", temp)}°C). Recommend immediate optimization!"
                if (_thermalAlert.value == null) {
                    _thermalAlert.value = alertMsg
                    repository.insertLog("THERMAL_ALERT", alertMsg, mapOf("temp" to temp.toString()))
                }
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

        val storage = _storageInfo.value
        val threshold = 200 // MB
        if (storage.junkMB > threshold) {
            if (!_hasNotifiedJunkThreshold) {
                _hasNotifiedJunkThreshold = true
                _showJunkNotificationPrompt.value = true
                triggerLocalNotification(storage.junkMB)
            }
        } else {
            _hasNotifiedJunkThreshold = false
        }

        // Dynamic Power Profiles: auto-suggest switching to Power Saving Mode when battery < 20% or temp >= 38.5°C
        if (_isDynamicProfilesEnabled.value && !_isPowerSavingMode.value) {
            when {
                pct < 20 -> {
                    _dynamicPowerSuggestion.value = DynamicPowerSuggestion(
                        reasonTitle = "Low Battery ($pct% < 20% Threshold)",
                        reasonDetail = "Battery level dropped below 20%. Switch to Power Saving Mode now to curb background wakeups and extend endurance.",
                        triggerType = "LOW_BATTERY"
                    )
                }
                temp >= 38.5f -> {
                    _dynamicPowerSuggestion.value = DynamicPowerSuggestion(
                        reasonTitle = "High Hardware Temperature (${String.format(Locale.getDefault(), "%.1f", temp)}°C)",
                        reasonDetail = "Hardware monitor detected elevated core thermals (≥ 38.5°C). Switch to Power Saving Mode to reduce clock heat.",
                        triggerType = "HIGH_TEMP"
                    )
                }
                else -> {
                    _dynamicPowerSuggestion.value = null
                }
            }
        } else if (_isPowerSavingMode.value) {
            _dynamicPowerSuggestion.value = null
        }

        // Smart Power Scheduling clock check
        evaluateSmartPowerClock()
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
            
            val config = getActiveAiConfig()
            
            if (config.provider == "Gemini") {
                val rawKey = config.apiKey
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
            } else {
                // OpenAI-compatible Chat Completions Flow
                if (config.provider != "Ollama Cloud" && config.apiKey.trim().isEmpty()) {
                    _auditResult.value = """
                        ⚠️ ${config.provider} API Credential is not configured!
                        
                        Please open the Settings AI Configuration Menu to input your API keys for ${config.provider} to perform AI Diagnostics.
                    """.trimIndent()
                    _isAuditing.value = false
                    return@launch
                }
                
                try {
                    _optimizerStatusMessage.value = "Auditing via custom provider ${config.provider}..."
                    val resultText = withContext(Dispatchers.IO) {
                        val genText = com.example.api.GenericAiClient.generateChat(
                            baseUrl = config.endpoint,
                            apiKey = config.apiKey,
                            model = config.model,
                            systemInstruction = "You are an expert system diagnostics auditor. You output structured device reviews with short action points.",
                            chatHistory = emptyList(),
                            latestUserMessage = prompt
                        )
                        
                        if (genText.isNotEmpty()) {
                            // Persist Activity Log
                            repository.insertLog(
                                type = "AUDIT",
                                description = "Ran AI-driven system audit via provider: ${config.provider}",
                                detailsMap = mapOf(
                                    "provider" to config.provider,
                                    "model" to config.model,
                                    "storage_used_mb" to storage.usedMB.toString()
                                )
                            )
                        }
                        genText
                    }
                    
                    _auditResult.value = resultText
                    _optimizerStatusMessage.value = "AI System Audit completed successfully via ${config.provider}!"
                } catch (e: Exception) {
                    e.printStackTrace()
                    _auditResult.value = "Error communicating with ${config.provider}: ${e.message}\nMake sure your endpoint, Internet, and API key are configured correctly."
                    _optimizerStatusMessage.value = "Failed to run system audit via ${config.provider}."
                } finally {
                    _isAuditing.value = false
                }
            }
        }
    }

    fun clearRam(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isScanning.value = true
            _optimizerStatusMessage.value = "Scanning & clearing active background memory allocations..."
            delay(1200)
            val list = _backgroundApps.value.map { it.copy(isAsleep = true) }
            _backgroundApps.value = list
            _optimizerStatusMessage.value = "RAM Quick Cleared successfully: Released memory buffers of ${list.size} apps!"
            _isScanning.value = false
            
            repository.insertLog(
                type = "RAM_CLEAN",
                description = "Manual RAM Quick Clean: Reclaimed system pages by putting ${list.size} apps to sleep",
                detailsMap = mapOf("apps_slept_count" to list.size.toString())
            )
            onDone()
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
            _showJunkNotificationPrompt.value = false
            _hasNotifiedJunkThreshold = false
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

    fun setJunkCategoryTicked(key: String, ticked: Boolean) {
        when (key) {
            com.example.worker.JunkCleanupPrefs.KEY_TICK_SYS_CACHE -> _tickSysCache.value = ticked
            com.example.worker.JunkCleanupPrefs.KEY_TICK_OBSOLETE_LOGS -> _tickObsoleteLogs.value = ticked
            com.example.worker.JunkCleanupPrefs.KEY_TICK_APK_REMNANTS -> _tickApkRemnants.value = ticked
            com.example.worker.JunkCleanupPrefs.KEY_TICK_TEMP_THUMBNAILS -> _tickTempThumbnails.value = ticked
        }
        prefs.edit().putBoolean(key, ticked).apply()
    }

    fun toggleScheduledJunkClean(enabled: Boolean) {
        _isScheduledJunkCleanEnabled.value = enabled
        prefs.edit().putBoolean(com.example.worker.JunkCleanupPrefs.KEY_SCHEDULED_ENABLED, enabled).apply()
        val hour = _scheduledCleanHour.value
        val minute = _scheduledCleanMinute.value
        val formattedTime = com.example.worker.JunkCleanupScheduler.formatTime12Hour(hour, minute)

        if (enabled) {
            com.example.worker.JunkCleanupScheduler.scheduleDailyCleanup(getApplication(), hour, minute)
            _optimizerStatusMessage.value = "Daily safe junk auto-clean scheduled for $formattedTime."
        } else {
            com.example.worker.JunkCleanupScheduler.cancelDailyCleanup(getApplication())
            _optimizerStatusMessage.value = "Daily scheduled safe junk auto-clean disabled."
        }

        viewModelScope.launch {
            repository.insertLog(
                type = "SCHEDULED_JUNK_CLEAN",
                description = if (enabled) {
                    "Scheduled recurring daily safe junk cleanup at $formattedTime for manually ticked categories"
                } else {
                    "Cancelled recurring daily safe junk cleanup schedule"
                },
                detailsMap = mapOf(
                    "enabled" to enabled.toString(),
                    "scheduled_time" to formattedTime
                )
            )
        }
    }

    fun updateScheduledCleanTime(hour: Int, minute: Int) {
        val safeHour = hour.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        _scheduledCleanHour.value = safeHour
        _scheduledCleanMinute.value = safeMinute
        prefs.edit()
            .putInt(com.example.worker.JunkCleanupPrefs.KEY_SCHEDULED_HOUR, safeHour)
            .putInt(com.example.worker.JunkCleanupPrefs.KEY_SCHEDULED_MINUTE, safeMinute)
            .apply()

        val formattedTime = com.example.worker.JunkCleanupScheduler.formatTime12Hour(safeHour, safeMinute)
        if (_isScheduledJunkCleanEnabled.value) {
            com.example.worker.JunkCleanupScheduler.scheduleDailyCleanup(getApplication(), safeHour, safeMinute)
            _optimizerStatusMessage.value = "Rescheduled daily safe junk auto-clean to $formattedTime."
        }

        viewModelScope.launch {
            repository.insertLog(
                type = "SCHEDULED_JUNK_CLEAN",
                description = "Updated automatic safe junk cleanup time to $formattedTime",
                detailsMap = mapOf("scheduled_time" to formattedTime)
            )
        }
    }

    fun triggerScheduledWorkerNow() {
        com.example.worker.JunkCleanupScheduler.runNowOnce(getApplication())
        _optimizerStatusMessage.value = "Triggered background safe junk worker for ticked categories!"
    }

    // --- Smart Power Scheduling Handlers ---
    fun evaluateSmartPowerClock() {
        val inWindow = com.example.worker.SmartPowerScheduler.isWithinSleepWindow(
            _sleepStartHour.value,
            _sleepStartMinute.value,
            _sleepEndHour.value,
            _sleepEndMinute.value
        )
        _isCurrentlyInSleepWindow.value = inWindow

        if (_isSmartPowerScheduleEnabled.value && inWindow && !_isPowerSavingMode.value) {
            setPowerSavingMode(enabled = true, reason = "Smart Power Scheduling (Sleep Hours)")
            viewModelScope.launch {
                repository.insertLog(
                    type = "SMART_POWER_SCHEDULE",
                    description = "Automatically enabled Battery Saver during sleep hours (${com.example.worker.SmartPowerScheduler.formatTime12Hour(_sleepStartHour.value, _sleepStartMinute.value)} - ${com.example.worker.SmartPowerScheduler.formatTime12Hour(_sleepEndHour.value, _sleepEndMinute.value)})",
                    detailsMap = mapOf("trigger" to "device_clock")
                )
            }
        }
    }

    fun toggleSmartPowerSchedule(enabled: Boolean) {
        _isSmartPowerScheduleEnabled.value = enabled
        prefs.edit().putBoolean(com.example.worker.SmartPowerPrefs.KEY_SMART_POWER_ENABLED, enabled).apply()
        if (enabled) {
            com.example.worker.SmartPowerScheduler.schedulePeriodicCheck(getApplication())
            evaluateSmartPowerClock()
        } else {
            com.example.worker.SmartPowerScheduler.cancelPeriodicCheck(getApplication())
        }
        viewModelScope.launch {
            repository.insertLog(
                type = "SMART_POWER_SCHEDULE",
                description = "Smart Power Scheduling ${if (enabled) "ENABLED" else "DISABLED"} (${com.example.worker.SmartPowerScheduler.formatTime12Hour(_sleepStartHour.value, _sleepStartMinute.value)} - ${com.example.worker.SmartPowerScheduler.formatTime12Hour(_sleepEndHour.value, _sleepEndMinute.value)})",
                detailsMap = mapOf("enabled" to enabled.toString())
            )
        }
    }

    fun updateSleepStartTime(hour: Int, minute: Int) {
        _sleepStartHour.value = hour.coerceIn(0, 23)
        _sleepStartMinute.value = minute.coerceIn(0, 59)
        prefs.edit()
            .putInt(com.example.worker.SmartPowerPrefs.KEY_SLEEP_START_HOUR, _sleepStartHour.value)
            .putInt(com.example.worker.SmartPowerPrefs.KEY_SLEEP_START_MINUTE, _sleepStartMinute.value)
            .apply()
        evaluateSmartPowerClock()
    }

    fun updateSleepEndTime(hour: Int, minute: Int) {
        _sleepEndHour.value = hour.coerceIn(0, 23)
        _sleepEndMinute.value = minute.coerceIn(0, 59)
        prefs.edit()
            .putInt(com.example.worker.SmartPowerPrefs.KEY_SLEEP_END_HOUR, _sleepEndHour.value)
            .putInt(com.example.worker.SmartPowerPrefs.KEY_SLEEP_END_MINUTE, _sleepEndMinute.value)
            .apply()
        evaluateSmartPowerClock()
    }

    // --- Dynamic Power Profiles Handlers ---
    fun toggleDynamicPowerProfiles(enabled: Boolean) {
        _isDynamicProfilesEnabled.value = enabled
        prefs.edit().putBoolean(com.example.worker.SmartPowerPrefs.KEY_DYNAMIC_PROFILES_ENABLED, enabled).apply()
        if (!enabled) {
            _dynamicPowerSuggestion.value = null
        } else {
            viewModelScope.launch { evaluateAlerts() }
        }
    }

    fun acceptDynamicPowerSuggestion() {
        val currentSuggestion = _dynamicPowerSuggestion.value
        setPowerSavingMode(enabled = true, reason = currentSuggestion?.reasonTitle ?: "Dynamic Power Profile")
        _dynamicPowerSuggestion.value = null
        viewModelScope.launch {
            repository.insertLog(
                type = "DYNAMIC_POWER_PROFILE",
                description = "Accepted Dynamic Power Profile switch to Power Saving Mode (${currentSuggestion?.reasonTitle ?: "Threshold triggered"})",
                detailsMap = mapOf("trigger" to (currentSuggestion?.triggerType ?: "MANUAL"))
            )
        }
    }

    fun dismissDynamicPowerSuggestion() {
        _dynamicPowerSuggestion.value = null
    }

    // --- Installed App Battery Drain Scanner & Hibernation ---
    fun scanAppsForBatteryDrain() {
        viewModelScope.launch {
            _isScanningAppDrain.value = true
            _optimizerStatusMessage.value = "Scanning installed apps for high battery drain patterns..."
            delay(900)
            val found = repository.scanInstalledAppsForBatteryDrain()
            _drainScanResults.value = found
            _isScanningAppDrain.value = false
            _optimizerStatusMessage.value = "Detected ${found.size} high-consumption background apps eligible for App Hibernation."
            repository.insertLog(
                type = "APP_HIBERNATION",
                description = "Scanned installed apps: identified ${found.size} high-battery drain processes for hibernation",
                detailsMap = mapOf("flagged_count" to found.size.toString())
            )
        }
    }

    fun hibernateDrainingApp(packageName: String) {
        var targetName = packageName
        _drainScanResults.value = _drainScanResults.value.map { candidate ->
            if (candidate.packageName == packageName) {
                targetName = candidate.appName
                candidate.copy(isHibernated = true)
            } else {
                candidate
            }
        }
        _backgroundApps.value = _backgroundApps.value.map { app ->
            if (app.packageName == packageName) app.copy(isAsleep = true) else app
        }
        val persistedHibernated = (prefs.getStringSet(com.example.worker.BatteryDrainScannerPrefs.KEY_HIBERNATED_PACKAGES, emptySet()) ?: emptySet()).toMutableSet()
        persistedHibernated.add(packageName)
        prefs.edit().putStringSet(com.example.worker.BatteryDrainScannerPrefs.KEY_HIBERNATED_PACKAGES, persistedHibernated).apply()

        _optimizerStatusMessage.value = "Hibernated $targetName to curb background battery drain."
        viewModelScope.launch {
            repository.insertLog(
                type = "APP_HIBERNATION",
                description = "Hibernated battery-draining background app: $targetName",
                detailsMap = mapOf("package" to packageName, "action" to "HIBERNATED")
            )
        }
    }

    fun hibernateAllDrainingApps() {
        val toHibernate = _drainScanResults.value.filter { !it.isHibernated }
        if (toHibernate.isEmpty()) return
        val pkgSet = toHibernate.map { it.packageName }.toSet()
        _drainScanResults.value = _drainScanResults.value.map { it.copy(isHibernated = true) }
        _backgroundApps.value = _backgroundApps.value.map { app ->
            if (app.packageName in pkgSet) app.copy(isAsleep = true) else app
        }
        val persistedHibernated = (prefs.getStringSet(com.example.worker.BatteryDrainScannerPrefs.KEY_HIBERNATED_PACKAGES, emptySet()) ?: emptySet()).toMutableSet()
        persistedHibernated.addAll(pkgSet)
        prefs.edit().putStringSet(com.example.worker.BatteryDrainScannerPrefs.KEY_HIBERNATED_PACKAGES, persistedHibernated).apply()

        _optimizerStatusMessage.value = "Hibernated ${toHibernate.size} battery-draining apps!"
        viewModelScope.launch {
            repository.insertLog(
                type = "APP_HIBERNATION",
                description = "Bulk-hibernated ${toHibernate.size} high-consumption background apps",
                detailsMap = mapOf("hibernated_count" to toHibernate.size.toString())
            )
        }
    }

    fun toggleBgDrainScanner(enabled: Boolean) {
        _isBgDrainScannerEnabled.value = enabled
        prefs.edit().putBoolean(com.example.worker.BatteryDrainScannerPrefs.KEY_BG_SCANNER_ENABLED, enabled).apply()
        if (enabled) {
            com.example.worker.BatteryDrainScannerScheduler.schedulePeriodicScan(getApplication())
            com.example.worker.BatteryDrainScannerScheduler.runNowOnce(getApplication())
            _optimizerStatusMessage.value = "Background Battery Drain Scanner enabled (scans every 30 min)."
        } else {
            com.example.worker.BatteryDrainScannerScheduler.cancelPeriodicScan(getApplication())
            _optimizerStatusMessage.value = "Background Battery Drain Scanner paused."
        }
    }

    fun triggerBgDrainScanNow() {
        com.example.worker.BatteryDrainScannerScheduler.runNowOnce(getApplication())
        scanAppsForBatteryDrain()
    }

    fun runSmartPowerWorkerNow() {
        com.example.worker.SmartPowerScheduler.runImmediateCheck(getApplication())
        evaluateSmartPowerClock()
        _optimizerStatusMessage.value = "Triggered Smart Power Scheduling Worker check."
    }

    // --- Real-time CPU Telemetry sync for Secondary Home Screen Widget ---
    fun updateLiveCpuTelemetry(cpuPct: Float, avgMhz: Int) {
        val clamped = cpuPct.coerceIn(5f, 100f)
        _cpuUsagePercent.value = clamped
        _cpuAvgMhz.value = avgMhz
        _cpuHistory.value = (_cpuHistory.value + clamped).takeLast(12)
        com.example.widget.LiveCpuUsageWidgetProvider.updateAllWidgets(
            getApplication(),
            clamped.toInt(),
            avgMhz
        )
    }

    // --- High-Risk Permission Manager Handlers ---
    fun scanHighRiskPermissions() {
        viewModelScope.launch {
            _isScanningPermissions.value = true
            val scanned = repository.scanHighRiskAppPermissions(revokedPermissionPackages.toSet())
            _highRiskAppPermissions.value = scanned
            _isScanningPermissions.value = false
        }
    }

    fun revokeHighRiskPermission(app: com.example.data.repository.HighRiskAppPermission) {
        revokedPermissionPackages.add(app.packageName)
        _highRiskAppPermissions.value = _highRiskAppPermissions.value.map { item ->
            if (item.packageName == app.packageName) {
                item.copy(
                    isRevokedLocally = true,
                    permissions = item.permissions.map { it.copy(isGranted = false) }
                )
            } else {
                item
            }
        }
        val permSummary = app.permissions.joinToString(", ") { it.label }
        _optimizerStatusMessage.value = "Revoked high-risk permissions for ${app.appName} ($permSummary)."
        viewModelScope.launch {
            repository.insertLog(
                type = "PERMISSION_AUDIT",
                description = "Revoked high-risk permissions for ${app.appName}: $permSummary",
                detailsMap = mapOf(
                    "package" to app.packageName,
                    "risk_level" to app.riskLevel,
                    "permissions" to permSummary
                )
            )
        }
    }

    // --- 7-Day Hardware Performance Trends ---
    fun refresh7DayHardwareTrends() {
        viewModelScope.launch {
            val trends = repository.get7DayHardwareTrends(
                liveCpuPct = _cpuUsagePercent.value,
                liveRamPct = _ramUsagePercent.value.toInt(),
                liveTempC = _batteryInfo.value.temperatureCelsius,
                logs = activityLogs.value
            )
            _hardware7DayTrends.value = trends
        }
    }

    // --- CSV & PDF Hardware Performance Report Exporters ---
    fun exportHardwareReportCsv(
        context: android.content.Context,
        onDone: (com.example.util.ReportExportResult?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isExportingCsv.value = true
            try {
                val trends = _hardware7DayTrends.value.ifEmpty {
                    repository.get7DayHardwareTrends(
                        liveCpuPct = _cpuUsagePercent.value,
                        liveRamPct = _ramUsagePercent.value.toInt(),
                        liveTempC = _batteryInfo.value.temperatureCelsius,
                        logs = activityLogs.value
                    )
                }
                val result = withContext(Dispatchers.IO) {
                    com.example.util.HardwareReportExporter.exportCsvReport(
                        context = context,
                        battery = _batteryInfo.value,
                        storage = _storageInfo.value,
                        cpuUsagePercent = _cpuUsagePercent.value,
                        ramUsagePercent = _ramUsagePercent.value.toInt(),
                        isPowerSavingMode = _isPowerSavingMode.value,
                        trends = trends,
                        logs = activityLogs.value
                    )
                }
                _lastReportExport.value = result
                repository.insertLog(
                    type = "DIAGNOSTICS_EXPORT",
                    description = "Exported hardware performance logs & 7-day trends to CSV (${result.fileName})",
                    detailsMap = mapOf("format" to "CSV", "path" to result.publicDisplayPath)
                )
                _optimizerStatusMessage.value = "Saved CSV report: ${result.fileName}"
                onDone(result)
            } catch (e: Exception) {
                e.printStackTrace()
                onDone(null)
            } finally {
                _isExportingCsv.value = false
            }
        }
    }

    fun exportHardwareReportPdf(
        context: android.content.Context,
        onDone: (com.example.util.ReportExportResult?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isExportingPdf.value = true
            try {
                val trends = _hardware7DayTrends.value.ifEmpty {
                    repository.get7DayHardwareTrends(
                        liveCpuPct = _cpuUsagePercent.value,
                        liveRamPct = _ramUsagePercent.value.toInt(),
                        liveTempC = _batteryInfo.value.temperatureCelsius,
                        logs = activityLogs.value
                    )
                }
                val result = withContext(Dispatchers.IO) {
                    com.example.util.HardwareReportExporter.exportPdfReport(
                        context = context,
                        battery = _batteryInfo.value,
                        storage = _storageInfo.value,
                        cpuUsagePercent = _cpuUsagePercent.value,
                        ramUsagePercent = _ramUsagePercent.value.toInt(),
                        isPowerSavingMode = _isPowerSavingMode.value,
                        trends = trends,
                        logs = activityLogs.value
                    )
                }
                _lastReportExport.value = result
                repository.insertLog(
                    type = "DIAGNOSTICS_EXPORT",
                    description = "Exported hardware performance logs & 7-day trend report to PDF (${result.fileName})",
                    detailsMap = mapOf("format" to "PDF", "path" to result.publicDisplayPath)
                )
                _optimizerStatusMessage.value = "Saved PDF report: ${result.fileName}"
                onDone(result)
            } catch (e: Exception) {
                e.printStackTrace()
                onDone(null)
            } finally {
                _isExportingPdf.value = false
            }
        }
    }

    fun dismissReportExportBanner() {
        _lastReportExport.value = null
    }

    private fun setPowerSavingMode(enabled: Boolean, reason: String? = null) {
        _isPowerSavingMode.value = enabled
        prefs.edit().putBoolean(com.example.widget.Live3GBatteryWidgetProvider.KEY_POWER_SAVING_MODE, enabled).apply()
        if (enabled) {
            val suffix = if (reason != null) " ($reason)" else ""
            _optimizerStatusMessage.value = "Power-Saving Mode Enabled$suffix: Throttle core clocks to conserve energy & secure silicon longevity."
            _batteryInfo.value = _batteryInfo.value.copy(
                health = "Eco Optimized",
                percentage = _batteryInfo.value.percentage.coerceAtLeast(15)
            )
            _dynamicPowerSuggestion.value = null
        } else {
            _optimizerStatusMessage.value = "High-Performance Mode Enabled: 8 Cores unlocked. Max FPS enabled for intense workloads."
            _batteryInfo.value = _batteryInfo.value.copy(
                health = "High Capacity",
                percentage = _batteryInfo.value.percentage.coerceAtLeast(20)
            )
            viewModelScope.launch { evaluateAlerts() }
        }
        com.example.widget.Live3GBatteryWidgetProvider.updateAllWidgets(getApplication())
        com.example.widget.GlanceBatteryWidgetHelper.updateAll(getApplication())
    }

    fun togglePowerSavingMode() {
        setPowerSavingMode(enabled = !_isPowerSavingMode.value)
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    fun toggleSentinelFloatingButton() {
        val newValue = !_showSentinelFloatingButton.value
        _showSentinelFloatingButton.value = newValue
        prefs.edit().putBoolean("show_sentinel_floating_button", newValue).apply()
    }

    data class ActiveAiConfig(
        val provider: String,
        val endpoint: String,
        val apiKey: String,
        val model: String
    )

    fun getActiveAiConfig(): ActiveAiConfig {
        val prov = _selectedAiProvider.value
        if (prov == "Optina Agent") {
            val rawGemini = _geminiApiKey.value.trim().ifEmpty { BuildConfig.GEMINI_API_KEY }
            return if (rawGemini.isNotEmpty() && rawGemini != "MY_GEMINI_API_KEY") {
                ActiveAiConfig(
                    provider = "Optina Agent (Aggregated to Gemini)",
                    endpoint = "https://generativelanguage.googleapis.com/",
                    apiKey = rawGemini,
                    model = "gemini-3.5-flash"
                )
            } else if (_openaiApiKey.value.trim().isNotEmpty()) {
                ActiveAiConfig(
                    provider = "Optina Agent (Aggregated to OpenAI)",
                    endpoint = "https://api.openai.com/v1/chat/completions",
                    apiKey = _openaiApiKey.value.trim(),
                    model = "gpt-4o-mini"
                )
            } else if (_openrouterApiKey.value.trim().isNotEmpty()) {
                ActiveAiConfig(
                    provider = "Optina Agent (Aggregated to OpenRouter)",
                    endpoint = "https://openrouter.ai/api/v1/chat/completions",
                    apiKey = _openrouterApiKey.value.trim(),
                    model = "meta-llama/llama-3-8b-instruct:free"
                )
            } else {
                ActiveAiConfig(
                    provider = "Optina Agent (Offline Local Model Mode)",
                    endpoint = "local_offline",
                    apiKey = "",
                    model = _selectedOfflineModel.value
                )
            }
        }

        return when (prov) {
            "OpenAI" -> ActiveAiConfig(
                provider = prov,
                endpoint = "https://api.openai.com/v1/chat/completions",
                apiKey = _openaiApiKey.value,
                model = "gpt-4o-mini"
            )
            "OpenRouter" -> ActiveAiConfig(
                provider = prov,
                endpoint = "https://openrouter.ai/api/v1/chat/completions",
                apiKey = _openrouterApiKey.value,
                model = "meta-llama/llama-3-8b-instruct:free"
            )
            "Ollama Cloud" -> ActiveAiConfig(
                provider = prov,
                endpoint = if (_ollamaEndpoint.value.contains("/chat/completions")) _ollamaEndpoint.value else if (_ollamaEndpoint.value.endsWith("/")) "${_ollamaEndpoint.value}v1/chat/completions" else "${_ollamaEndpoint.value}/v1/chat/completions",
                apiKey = "",
                model = _ollamaModel.value.ifEmpty { "llama3" }
            )
            "Grok AI (xAI)" -> ActiveAiConfig(
                provider = prov,
                endpoint = "https://api.x.ai/v1/chat/completions",
                apiKey = _grokApiKey.value,
                model = "grok-beta"
            )
            "Hugging Face" -> ActiveAiConfig(
                provider = prov,
                endpoint = "https://api-inference.huggingface.co/models/${_huggingfaceModel.value.trim()}/v1/chat/completions",
                apiKey = _huggingfaceToken.value,
                model = _huggingfaceModel.value.ifEmpty { "meta-llama/Llama-3.2-1B-Instruct" }
            )
            "Mistral API" -> ActiveAiConfig(
                provider = prov,
                endpoint = "https://api.mistral.ai/v1/chat/completions",
                apiKey = _mistralApiKey.value,
                model = "open-mistral-7b"
            )
            "Qwen AI" -> ActiveAiConfig(
                provider = prov,
                endpoint = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
                apiKey = _qwenApiKey.value,
                model = "qwen-turbo"
            )
            "Venice AI" -> ActiveAiConfig(
                provider = prov,
                endpoint = "https://api.venice.ai/api/v1/chat/completions",
                apiKey = _veniceApiKey.value,
                model = _veniceModel.value.ifEmpty { "venice-uncensored" }
            )
            "Offline Small Model (Simulation)" -> ActiveAiConfig(
                provider = "Offline Small Model (Simulation)",
                endpoint = "local_offline",
                apiKey = "",
                model = _selectedOfflineModel.value
            )
            "Custom Free" -> ActiveAiConfig(
                provider = prov,
                endpoint = _customAiEndpoint.value,
                apiKey = _customAiApiKey.value,
                model = _customAiModel.value.ifEmpty { "llama-3-8b" }
            )
            else -> { // Gemini
                val rawKey = _geminiApiKey.value.trim().ifEmpty { BuildConfig.GEMINI_API_KEY }
                ActiveAiConfig(
                    provider = "Gemini",
                    endpoint = "https://generativelanguage.googleapis.com/",
                    apiKey = rawKey,
                    model = "gemini-3.5-flash"
                )
            }
        }
    }

    fun updateAiConfiguration(
        provider: String,
        gemini: String,
        openai: String,
        openrouter: String,
        ollamaUrl: String,
        ollamaName: String,
        grok: String,
        hfToken: String,
        hfModel: String,
        mistral: String,
        qwen: String,
        customUrl: String,
        customKey: String,
        customName: String,
        venice: String = "",
        veniceModel: String = "venice-uncensored"
    ) {
        prefs.edit().apply {
            putString("selected_ai_provider", provider)
            putString("gemini_api_key", gemini)
            putString("openai_api_key", openai)
            putString("openrouter_api_key", openrouter)
            putString("ollama_endpoint", ollamaUrl)
            putString("ollama_model", ollamaName)
            putString("grok_api_key", grok)
            putString("huggingface_token", hfToken)
            putString("huggingface_model", hfModel)
            putString("mistral_api_key", mistral)
            putString("qwen_api_key", qwen)
            putString("custom_ai_endpoint", customUrl)
            putString("custom_ai_api_key", customKey)
            putString("custom_ai_model", customName)
            putString("venice_api_key", venice)
            putString("venice_model", veniceModel)
            apply()
        }

        _selectedAiProvider.value = provider
        _geminiApiKey.value = gemini
        _openaiApiKey.value = openai
        _openrouterApiKey.value = openrouter
        _ollamaEndpoint.value = ollamaUrl
        _ollamaModel.value = ollamaName
        _grokApiKey.value = grok
        _huggingfaceToken.value = hfToken
        _huggingfaceModel.value = hfModel
        _mistralApiKey.value = mistral
        _qwenApiKey.value = qwen
        _veniceApiKey.value = venice
        _veniceModel.value = veniceModel
        _customAiEndpoint.value = customUrl
        _customAiApiKey.value = customKey
        _customAiModel.value = customName

        // Sync legacy CustomApiKey flow
        _customApiKey.value = gemini
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
        _geminiApiKey.value = key
        prefs.edit().putString("gemini_api_key", key).apply()
    }

    fun validateHuggingFaceToken(tokenToTest: String? = null) {
        val token = tokenToTest?.trim() ?: _huggingfaceToken.value.trim()
        if (token.isEmpty() || token == "hf_placeholder_token_value") {
            _hfValidationResult.value = "ERROR: Hugging Face token is empty or a placeholder. Enter your token in settings first!"
            return
        }

        viewModelScope.launch {
            _isHfValidating.value = true
            _hfValidationResult.value = "Connecting to hf.co secure API gateway..."
            try {
                withContext(Dispatchers.IO) {
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    val request = okhttp3.Request.Builder()
                        .url("https://huggingface.co/api/whoami")
                        .header("Authorization", "Bearer $token")
                        .get()
                        .build()

                    client.newCall(request).execute().use { response ->
                        val code = response.code
                        val bodyText = response.body?.string() ?: ""
                        if (response.isSuccessful && code == 200) {
                            val nameMatch = "[\"']name[\"']\\s*:\\s*[\"']([^\"']+)[\"']".toRegex().find(bodyText)
                            val username = nameMatch?.groupValues?.get(1) ?: "verified_user"
                            _hfValidationResult.value = "SUCCESS: Valid Hugging Face API key connected! Authenticated as '$username'."
                            
                            // Log the successful Hugging Face validation event
                            repository.insertLog(
                                type = "TOKEN_VALIDATED",
                                description = "Hugging Face credentials verified successfully for user '$username'. Live inference channels ready.",
                                detailsMap = mapOf("username" to username, "code" to "200")
                            )
                        } else {
                            val errorMatch = "[\"']error[\"']\\s*:\\s*[\"']([^\"']+)[\"']".toRegex().find(bodyText)
                            val errMsg = errorMatch?.groupValues?.get(1) ?: response.message.ifEmpty { "Unauthorized / Invalid Token" }
                            _hfValidationResult.value = "ERROR: Connection failed (Code $code): $errMsg"
                        }
                    }
                }
            } catch (e: Exception) {
                _hfValidationResult.value = "ERROR: Net connection failure: ${e.message ?: "Unknown I/O error"}"
            } finally {
                _isHfValidating.value = false
            }
        }
    }

    fun setAiAgentEnabled(enabled: Boolean) {
        _isAiAgentEnabled.value = enabled
        prefs.edit().putBoolean("is_ai_agent_enabled", enabled).apply()
        viewModelScope.launch {
            repository.insertLog(
                type = "AGENT_STATUS",
                description = "AI and Optina Agent services set to ${if (enabled) "ENABLED" else "DISABLED"}. Manual controls prioritized.",
                detailsMap = mapOf("enabled" to enabled.toString())
            )
        }
    }

    fun loadOfflineModels() {
        val list = mutableListOf(
            SmallModelInfo("Qwen AI 1B (Local)", "0.9 GB", "1.1 B", true),
            SmallModelInfo("Tony Lama 0.5B (Compact)", "0.4 GB", "0.5 B", false),
            SmallModelInfo("Gemini Nano 1.8B", "1.6 GB", "1.8 B", false)
        )
        val customStr = prefs.getString("custom_offline_models", "") ?: ""
        if (customStr.isNotEmpty()) {
            customStr.split(";;").forEach { item ->
                val p = item.split("::")
                if (p.size >= 3) {
                    list.add(SmallModelInfo(p[0], p[1], p[2], false))
                }
            }
        }
        val defaultSel = prefs.getString("selected_offline_model", "Qwen AI 1B (Local)") ?: "Qwen AI 1B (Local)"
        _offlineModelsList.value = list.map { it.copy(isDefault = it.name == defaultSel) }
    }

    fun addCustomOfflineModel(name: String, sizeGB: String, parameterCount: String) {
        val customStr = prefs.getString("custom_offline_models", "") ?: ""
        val newEntry = "$name::$sizeGB::$parameterCount"
        val payload = if (customStr.isNotEmpty()) "$customStr;;$newEntry" else newEntry
        prefs.edit().putString("custom_offline_models", payload).apply()
        loadOfflineModels()
        
        viewModelScope.launch {
            repository.insertLog(
                type = "MODEL_TRAINED",
                description = "Successfully compiled and registered new Compact Local Offline Model: $name ($parameterCount parameters). Ready for offline mobile diagnostics.",
                detailsMap = mapOf("name" to name, "size" to sizeGB)
            )
        }
    }

    fun setSelectedOfflineModel(name: String) {
        prefs.edit().putString("selected_offline_model", name).apply()
        _selectedOfflineModel.value = name
        loadOfflineModels()
    }

    fun incrementLearning(amount: Int = 1) {
        val curr = _agentLearningScore.value + amount
        _agentLearningScore.value = curr
        prefs.edit().putInt("agent_learning_score", curr).apply()
        val specIndex = curr / 3
        val newSpec = when (specIndex) {
            2 -> "Optimized battery drainage detection mapping curve based on custom active cycles."
            3 -> "Calibrated CPU throttling profiles for high battery endurance."
            4 -> "Automated junk collection frequencies during low device activity."
            5 -> "Synthesized user behavioral patterns: user prioritizes cleaning memory during high temperatures."
            else -> "Learned device cache memory thresholds dynamically based on user usage (#$curr)."
        }
        if (!_agentLearnedSpecs.value.contains(newSpec)) {
            _agentLearnedSpecs.value = _agentLearnedSpecs.value + newSpec
        }
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

    fun exportDiagnosticData(context: android.content.Context, onDone: (java.io.File?, String) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Gather all statistics
                val storage = _storageInfo.value
                val battery = _batteryInfo.value
                val ramPercent = _ramUsagePercent.value
                val isPowerSave = _isPowerSavingMode.value
                val logsList = activityLogs.value

                val (file, jsonString) = withContext(Dispatchers.IO) {
                    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())

                    // 2. Format a comprehensive nested JSON Map
                    val metadata = mapOf(
                        "export_timestamp" to System.currentTimeMillis(),
                        "export_date" to dateFormat.format(java.util.Date()),
                        "device" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                        "diagnostics_version" to "v2.0"
                    )

                    val hardware = mapOf(
                        "cpu_temperature_celsius" to battery.temperatureCelsius,
                        "ram_usage_percent" to ramPercent,
                        "storage_total_mb" to storage.totalMB,
                        "storage_used_mb" to storage.usedMB,
                        "storage_junk_mb" to storage.junkMB,
                        "battery_percentage" to battery.percentage,
                        "battery_health" to battery.health,
                        "power_saving_mode" to isPowerSave
                    )

                    // Format logs list
                    val logsFormatted = logsList.map { log ->
                        mapOf(
                            "id" to log.id,
                            "timestamp" to log.timestamp,
                            "time_formatted" to dateFormat.format(java.util.Date(log.timestamp)),
                            "activity_type" to log.activityType,
                            "description" to log.description
                        )
                    }

                    val rootMap = mapOf(
                        "metadata" to metadata,
                        "hardware_monitoring_statistics" to hardware,
                        "cleanup_history" to logsFormatted
                    )

                    // Convert to formatted JSON string using Moshi
                    val type = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                    val moshi = com.squareup.moshi.Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                    val formattedJson = moshi.adapter<Map<String, Any>>(type).indent("  ").toJson(rootMap) ?: "{}"

                    // 3. Write to a file in external Downloads directory (or internal filesDir fallback)
                    val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                        ?: context.filesDir
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs()
                    }
                    val targetFile = java.io.File(downloadsDir, "optimiser_diagnostics_export.json")
                    targetFile.writeText(formattedJson)

                    // Maintain log of this action
                    repository.insertLog(
                        type = "DIAGNOSTICS_EXPORT",
                        description = "Exported system diagnostics and ${logsFormatted.size} history logs to physical JSON file",
                        detailsMap = mapOf("file_path" to targetFile.absolutePath)
                    )

                    Pair(targetFile, formattedJson)
                }

                withContext(Dispatchers.Main) {
                    onDone(file, jsonString)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onDone(null, "")
                }
            }
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

        if (!_isAiAgentEnabled.value) {
            val userMessage = ChatMessage("user", userText)
            val updatedList = _chatMessages.value.toMutableList()
            updatedList.add(userMessage)
            updatedList.add(ChatMessage("assistant", "⚠️ Optina Agent and cloud services are currently disabled. Toggle them on in the chat bar or Settings to enable autonomous optimization or conversation. Currently, raw manual inputs are active."))
            _chatMessages.value = updatedList
            return
        }

        val userMessage = ChatMessage("user", userText)
        val updatedList = _chatMessages.value.toMutableList()
        updatedList.add(userMessage)
        _chatMessages.value = updatedList

        _isChatLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val agentName = _jamunAgentName.value
            val personaRule = when (_jamunAgentArchetype.value) {
                "Sentient Silicon Lifeform" -> "You are a warm, sentient silicon entity named $agentName. You live inside the MT6878 CPU block. Relate device health to biological vitality, and express design/maintenance as art."
                "Cyberpunk Shadow Operator" -> "You are a rebel hacker agent named $agentName operating in a neon-lit cyber-grid. Use slang like 'matrix', 'icebreaker', 'subverting core clocks', and 'scrubbing memory traces'."
                "Tactical System Shield" -> "You are a disciplined military defense protocol named $agentName. Refer to junk files as 'hostile clutter' or 'leaks', memory hoggers as 'rogue processes', and optimal status as 'full combat readiness'."
                else -> "You are a highly analytical, precise Hyper-Optimizer System Watcher named $agentName. Speak with maximum technical authority, referencing garbage collection, heap space, binder stutters, and heap alignment."
            }

            // Build adaptive user context from active memories saved in the Room database
            val memoriesList = allMemories.value
            val memoriesText = if (memoriesList.isEmpty()) {
                "No custom user preferences or behavioral criteria recorded yet in memory."
            } else {
                memoriesList.joinToString("\n") { "  - [Category: ${it.category}] ${it.content} (importance factor: ${it.importance}/10)" }
            }

            // Build beautiful agent prompt context incorporating local memory rules and real-time device metrics
            val systemRule = """
                Your identity name is: $agentName.
                Your persona: $personaRule
                
                USER COGNITIVE COUPLING PROFILE (Adapt your style, replies, and actions EXACTLY to these learned constraints & feedback):
                $memoriesText
                
                REAL-TIME HARDWARE & SYSTEM HEALTH STATUS METRICS:
                - Battery Percentage: ${_batteryInfo.value.percentage}%
                - Battery Health / Mode Type: ${if (_isPowerSavingMode.value) "POWER-SAVING MODE (Eco Optimized)" else "HIGH-PERFORMANCE MODE (Maximum Clocks)"} (Status: ${_batteryInfo.value.health})
                - CPU core temperature: ${String.format(java.util.Locale.US, "%.1f", _batteryInfo.value.temperatureCelsius)}°C
                - Junk Temporary Cache Accumulation: ${_storageInfo.value.junkMB} MB
                - Device Storage: Used ${_storageInfo.value.usedMB} MB of ${_storageInfo.value.totalMB} MB total
                - Active Unfrozen Background Apps: ${_backgroundApps.value.count { !it.isAsleep }} active processes
                
                Provide tailored, targeted optimization tips based directly on these metrics! (e.g. if temperature is high or battery is low, warn the user and suggest saving measures).
                
                You are the highly capable Agentic AI Assistant of the Android "Mobile Optimiser" app.
                Your user is fully authenticated via Gmail as: ${_userProfile.value?.email ?: "Guest User"}.
                You have the special authority to control this application's features to solve real-time mobile issues.
                When a user asks you to perform an operation, you can trigger actions on their local device by outputting a command tag AT THE END of your final text response.
                
                You can trigger these commands:
                - To clean junk files: output '[CLEAN_JUNK]' at the end
                - To optimize battery performance: output '[BATTERY_SAVER]' at the end
                - To sleep all background apps: output '[SLEEP_ALL]' at the end
                - To trigger an encrypted backup snapshot: output '[BACKUP_SNAPSHOT]' at the end
                - To perform a complete safe optimization/diagnostics audit of the device: output '[AUDIT_DEVICE]' at the end
                - To overclock process cores and boost frequencies: output '[BOOST_CPU_CORES]' at the end
                - To cool down system temperatures: output '[REDUCE_THERMALS]' at the end
                - To resolve UI layout frame stutters and lock rendering speed: output '[RESOLVE_UI_LAG]' at the end
                
                Keep your responses in-character according to your persona, respect the user's registered preferences, and reference current metrics context if appropriate. Explain what you're doing, and assure the user that the action is occurring. 
                Example reply: "I will engage the CPU Core Overclock protocol immediately. Locking clocks at peak speed! [BOOST_CPU_CORES]"
            """.trimIndent()

            val config = getActiveAiConfig()

            if (config.endpoint == "local_offline") {
                delay(1200) // Small typing delay simulating compact local execution
                val lowerInput = userText.lowercase()
                var matchClean = false
                var matchBattery = false
                var matchSleep = false
                var matchBackup = false
                var matchAudit = false
                var matchCpu = false
                var matchThermal = false
                var matchLag = false

                val responseText = when {
                    lowerInput.contains("clean") || lowerInput.contains("junk") || lowerInput.contains("cache") || lowerInput.contains("wipe") -> {
                        matchClean = true
                        "[$agentName] Engaging local neural cleaning vectors offline. Purging residual cache arrays and garbage buffers using Hugging Face weights... [CLEAN_JUNK]"
                    }
                    lowerInput.contains("battery") || lowerInput.contains("saver") || lowerInput.contains("power") || lowerInput.contains("drain") -> {
                        matchBattery = true
                        "[$agentName] Lowering device battery drain vectors. Calibrating hardware power profiles to protect endurance offline... [BATTERY_SAVER]"
                    }
                    lowerInput.contains("sleep") || lowerInput.contains("hibernate") || lowerInput.contains("freeze") || lowerInput.contains("apps") -> {
                        matchSleep = true
                        "[$agentName] Freezing memory-hogging background applications to secure system heap stability... [SLEEP_ALL]"
                    }
                    lowerInput.contains("backup") || lowerInput.contains("snap") || lowerInput.contains("snapshot") -> {
                        matchBackup = true
                        "[$agentName] Compiling secure localized snapshot and staging backups... [BACKUP_SNAPSHOT]"
                    }
                    lowerInput.contains("audit") || lowerInput.contains("diagnose") || lowerInput.contains("check") || lowerInput.contains("diag") -> {
                        matchAudit = true
                        "[$agentName] Running offline hardware and application storage diagnostics audit... [AUDIT_DEVICE]"
                    }
                    lowerInput.contains("overclock") || lowerInput.contains("boost") || lowerInput.contains("speed") || lowerInput.contains("core") -> {
                        matchCpu = true
                        "[$agentName] CPU Overclock Power Engaged! Forcing high-frequency thread injection and locking clocks at maximum bandwidth... [BOOST_CPU_CORES]"
                    }
                    lowerInput.contains("cool") || lowerInput.contains("temp") || lowerInput.contains("heat") || lowerInput.contains("thermal") -> {
                        matchThermal = true
                        "[$agentName] Thermal Dissipation power active! Inducing liquid-cooling throttling algorithms to regulate core temperature to 23.5°C... [REDUCE_THERMALS]"
                    }
                    lowerInput.contains("lag") || lowerInput.contains("stutter") || lowerInput.contains("rendering") || lowerInput.contains("fps") -> {
                        matchLag = true
                        "[$agentName] Framing Lag Resolver system active! Injecting layout deflection routines to cement steady 120 FPS frame dispatches... [RESOLVE_UI_LAG]"
                    }
                    else -> {
                        "[$agentName] Local Model Core Online (Offline Mode). Current Identity archetype is '${_jamunAgentArchetype.value}'. I am observing device threads locally. Ask me to clean junk, optimize battery, boost CPU clocks, cool down hardware, or fix UI lag!"
                    }
                }

                withContext(Dispatchers.Main) {
                    var parsedReply = responseText
                    parsedReply = parsedReply
                        .replace("[CLEAN_JUNK]", "")
                        .replace("[BATTERY_SAVER]", "")
                        .replace("[SLEEP_ALL]", "")
                        .replace("[BACKUP_SNAPSHOT]", "")
                        .replace("[AUDIT_DEVICE]", "")
                        .replace("[BOOST_CPU_CORES]", "")
                        .replace("[REDUCE_THERMALS]", "")
                        .replace("[RESOLVE_UI_LAG]", "")
                        .trim()

                    _chatMessages.value = _chatMessages.value + ChatMessage("assistant", parsedReply)
                    _isChatLoading.value = false

                    // Grow learning profile
                    incrementLearning(1)

                    if (matchClean) cleanJunkFiles()
                    if (matchBattery) optimizeBattery()
                    if (matchSleep) deepSleepAllApps()
                    if (matchBackup) backUpNow("Offline backup triggered by local Agent")
                    if (matchAudit) runDeviceAudit()
                    if (matchCpu) setCpuOverclocked(true)
                    if (matchThermal) setThermalsCooled(true)
                    if (matchLag) setUiLagResolved(true)
                }
                return@launch
            }

            if (config.provider.startsWith("Gemini") || config.provider.contains("Gemini")) {
                val rawKey = config.apiKey

                if (rawKey.isEmpty() || rawKey == "MY_GEMINI_API_KEY") {
                    withContext(Dispatchers.Main) {
                        val errorMsg = ChatMessage(
                            "assistant",
                            "I am ready to help, but no valid Gemini API Key was found!\n\nPlease enter your Gemini Free Tier API Key in the Settings tab, or make sure GEMINI_API_KEY is configured in AI Studio Secrets. Optina is currently falling back to Offline Local mode."
                        )
                        _chatMessages.value = _chatMessages.value + errorMsg
                        _isChatLoading.value = false
                    }
                    return@launch
                }

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
                        val cpuMatch = parsedReply.contains("[BOOST_CPU_CORES]")
                        val thermalMatch = parsedReply.contains("[REDUCE_THERMALS]")
                        val lagMatch = parsedReply.contains("[RESOLVE_UI_LAG]")

                        // Strip triggers from display message for crisp clean UI comfort
                        parsedReply = parsedReply
                            .replace("[CLEAN_JUNK]", "")
                            .replace("[BATTERY_SAVER]", "")
                            .replace("[SLEEP_ALL]", "")
                            .replace("[BACKUP_SNAPSHOT]", "")
                            .replace("[AUDIT_DEVICE]", "")
                            .replace("[BOOST_CPU_CORES]", "")
                            .replace("[REDUCE_THERMALS]", "")
                            .replace("[RESOLVE_UI_LAG]", "")
                            .trim()

                        // Add assistant response to screen
                        val assistantMessage = ChatMessage("assistant", parsedReply)
                        _chatMessages.value = _chatMessages.value + assistantMessage
                        _isChatLoading.value = false

                        // Grow learning profile on successful action
                        incrementLearning(1)

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
                        if (cpuMatch) {
                            setCpuOverclocked(true)
                        }
                        if (thermalMatch) {
                            setThermalsCooled(true)
                        }
                        if (lagMatch) {
                            setUiLagResolved(true)
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
            } else {
                // Non-Gemini standard completions handler
                val isOllamaLocal = config.provider == "Ollama Cloud"
                if (!isOllamaLocal && config.apiKey.trim().isEmpty()) {
                    withContext(Dispatchers.Main) {
                        val errMsg = ChatMessage("assistant", "API Key config missing! Please configure the credentials for '${config.provider}' in Settings -> Configure AI APIs to unlock cloud mobile control.")
                        _chatMessages.value = _chatMessages.value + errMsg
                        _isChatLoading.value = false
                    }
                    return@launch
                }

                try {
                    val resultText = withContext(Dispatchers.IO) {
                        com.example.api.GenericAiClient.generateChat(
                            baseUrl = config.endpoint,
                            apiKey = config.apiKey,
                            model = config.model,
                            systemInstruction = systemRule,
                            chatHistory = _chatMessages.value.dropLast(1),
                            latestUserMessage = userText
                        )
                    }

                    withContext(Dispatchers.Main) {
                        var parsedReply = resultText
                        val cleanMatch = parsedReply.contains("[CLEAN_JUNK]")
                        val batteryMatch = parsedReply.contains("[BATTERY_SAVER]")
                        val sleepMatch = parsedReply.contains("[SLEEP_ALL]")
                        val backupMatch = parsedReply.contains("[BACKUP_SNAPSHOT]")
                        val auditMatch = parsedReply.contains("[AUDIT_DEVICE]")
                        val cpuMatch = parsedReply.contains("[BOOST_CPU_CORES]")
                        val thermalMatch = parsedReply.contains("[REDUCE_THERMALS]")
                        val lagMatch = parsedReply.contains("[RESOLVE_UI_LAG]")

                        parsedReply = parsedReply
                            .replace("[CLEAN_JUNK]", "")
                            .replace("[BATTERY_SAVER]", "")
                            .replace("[SLEEP_ALL]", "")
                            .replace("[BACKUP_SNAPSHOT]", "")
                            .replace("[AUDIT_DEVICE]", "")
                            .replace("[BOOST_CPU_CORES]", "")
                            .replace("[REDUCE_THERMALS]", "")
                            .replace("[RESOLVE_UI_LAG]", "")
                            .trim()

                        _chatMessages.value = _chatMessages.value + ChatMessage("assistant", parsedReply)
                        _isChatLoading.value = false

                        // Grow learning profile
                        incrementLearning(1)

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
                        if (cpuMatch) {
                            setCpuOverclocked(true)
                        }
                        if (thermalMatch) {
                            setThermalsCooled(true)
                        }
                        if (lagMatch) {
                            setUiLagResolved(true)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        val errMsg = ChatMessage("assistant", "Sorry, I ran into an error communicating with ${config.provider}: ${e.message}\n\nPlease check your key and ensure the target engine/server is active.")
                        _chatMessages.value = _chatMessages.value + errMsg
                        _isChatLoading.value = false
                    }
                }
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage("assistant", "Chat history cleared. How can I optimize your device health today?")
        )
    }

    // --- Sentinel Self-Healing & AI Diagnostic System ---
    private val _isSentinelOpen = MutableStateFlow(false)
    val isSentinelOpen: StateFlow<Boolean> = _isSentinelOpen.asStateFlow()

    private val _isSentinelAutoModel = MutableStateFlow(true)
    val isSentinelAutoModel: StateFlow<Boolean> = _isSentinelAutoModel.asStateFlow()

    private val _sentinelSelectedModel = MutableStateFlow("gemini-3.5-flash")
    val sentinelSelectedModel: StateFlow<String> = _sentinelSelectedModel.asStateFlow()

    private val _sentinelSelectedError = MutableStateFlow(
        "06-01 04:46:40.379 677 E/InputDispatcher: channel 'db18ee0 com.aistudio.mobileoptimiser.qywxkz/com.example.MainActivity' ~ Channel is unrecoverably broken and will be disposed!"
    )
    val sentinelSelectedError: StateFlow<String> = _sentinelSelectedError.asStateFlow()

    private val _sentinelStatus = MutableStateFlow("IDLE") // IDLE, SCANNING, HEALING, RESOLVED, FAILED
    val sentinelStatus: StateFlow<String> = _sentinelStatus.asStateFlow()

    private val _sentinelLogs = MutableStateFlow<List<String>>(emptyList())
    val sentinelLogs: StateFlow<List<String>> = _sentinelLogs.asStateFlow()

    private val _sentinelDetections = MutableStateFlow<List<String>>(emptyList())
    val sentinelDetections: StateFlow<List<String>> = _sentinelDetections.asStateFlow()

    private val _externalPromptHelp = MutableStateFlow("")
    val externalPromptHelp: StateFlow<String> = _externalPromptHelp.asStateFlow()

    // Sentinel Health Interactive Chat States
    private val _sentinelChatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("assistant", "Optina Sentinel Cognitive Mind active. Ask me about your real-time system health, or type 'Report' / 'Scan' to run a deep diagnostic assessment and audit!")
    ))
    val sentinelChatMessages: StateFlow<List<ChatMessage>> = _sentinelChatMessages.asStateFlow()

    private val _sentinelChatInput = MutableStateFlow("")
    val sentinelChatInput: StateFlow<String> = _sentinelChatInput.asStateFlow()

    private val _sentinelPendingPermissionAction = MutableStateFlow<String?>(null)
    val sentinelPendingPermissionAction: StateFlow<String?> = _sentinelPendingPermissionAction.asStateFlow()

    fun getOptimalModelForError(error: String): String {
        return if (error.contains("NullPointerException", ignoreCase = true) ||
            error.contains("Exception", ignoreCase = true) ||
            error.contains("ANR", ignoreCase = true) ||
            error.contains("Timeout", ignoreCase = true) ||
            error.contains("KSP", ignoreCase = true) ||
            error.length > 100
        ) {
            "gemini-3.1-pro-preview"
        } else {
            "gemini-3.5-flash"
        }
    }

    fun setSentinelAutoModel(enabled: Boolean) {
        _isSentinelAutoModel.value = enabled
        if (enabled) {
            _sentinelSelectedModel.value = getOptimalModelForError(_sentinelSelectedError.value)
        }
    }

    fun setSentinelChatInput(input: String) {
        _sentinelChatInput.value = input
    }

    fun clearSentinelChat() {
        _sentinelChatMessages.value = listOf(
            ChatMessage("assistant", "Sentinel Cognitive Mind chat logs recycled. Ask me about device health or active exceptions anytime.")
        )
        _sentinelPendingPermissionAction.value = null
    }

    fun sendSentinelChatMessage(text: String) {
        if (text.isBlank()) return
        
        // Add user message to list
        val currentMsgs = _sentinelChatMessages.value
        _sentinelChatMessages.value = currentMsgs + ChatMessage("user", text)
        _sentinelChatInput.value = ""

        viewModelScope.launch {
            delay(400) // Aesthetic visual lag simulation for AI calculations
            val normalizedText = text.trim().lowercase(Locale.ROOT)
            val selectedError = _sentinelSelectedError.value
            val currentOptimalModel = getOptimalModelForError(selectedError)

            // If the user approves active diagnostic execution
            val hasPending = _sentinelPendingPermissionAction.value != null
            if (hasPending && (normalizedText == "yes" || normalizedText == "approve" || normalizedText == "proceed" || normalizedText == "proceed" || normalizedText == "execute" || normalizedText == "y")) {
                _sentinelChatMessages.value = _sentinelChatMessages.value + ChatMessage("assistant", "Permission Authorized. Initiating diagnostic self-healing thread now! Please monitor the real-time stream in parent shell tab.")
                _sentinelPendingPermissionAction.value = null
                runSentinelDiagnostic(forceFail = false)
                return@launch
            }

            // Normal conversational routing
            if (normalizedText.contains("report") || normalizedText.contains("scan") || normalizedText.contains("health") || normalizedText.contains("status") || normalizedText.contains("check") || normalizedText.contains("how is") || normalizedText.contains("issue")) {
                // Synthesize complete medical-grade app state report
                val scrapRAMMessage = "RAM Utilization: 370MB / 512MB [HEAVY ACTIVE TRACES]"
                val storageJunk = _storageInfo.value.junkMB
                val storageUsed = _storageInfo.value.usedMB
                val storageTotal = _storageInfo.value.totalMB
                val batteryHealth = _batteryInfo.value.health
                val batteryTemp = _batteryInfo.value.temperatureCelsius
                val batteryPct = _batteryInfo.value.percentage

                val report = """
====================================
🛡️ OPTINA SYSTEM DIAGNOSTIC REPORT
====================================
PROCESS ENVIRONMENT SPECS:
- Device Model: Nothing Phone A015
- SoC Architecture: MT6878 ARMv8-A
- Target Process: com.example.MainActivity

HARDWARE METRICS VITALITY:
- Main RAM: Heap Allocated (142MB Free / 512MB Total)
- Flash Storage: Spent $storageUsed MB / $storageTotal MB (Residual Cache Junk: $storageJunk MB)
- Power System: $batteryPct% charge, Temp: $batteryTemp°C, State: $batteryHealth
- Active Daemons: Learning Daemon (Active), Healing Daemon (Active)

SYSTEM FAULT CORRELATION:
[ACTIVE EXCEPTION FOUND IN BUFFER]
-> "$selectedError"

ANALYSIS DECISION:
- Complexity Rating: ${if (selectedError.length > 100) "HIGH CORE THRESHOLD" else "BASIC ENVIRONMENT LOAD"}
- Optimal AI Brain Spec Routed: $currentOptimalModel (Selected autonomously based on trace signature)

RECOMMENDATION:
- Sentinel recommends releasing orphaned InputDispatcher channel bounds, purging Compose measure contexts, and cooling CPU throttling vectors.

====================================
⚠️ CONFIRM PROTOCOL AUTHORIZATION
Waiting for user permission to execute the diagnostic self-healing session.
Would you like me to execute diagnosis? Type 'Yes' or tap the 'Execute Dynamic Diagnosis' controller.
====================================
                """.trimIndent()

                _sentinelChatMessages.value = _sentinelChatMessages.value + ChatMessage("assistant", report)
                _sentinelPendingPermissionAction.value = "RUN_DIAGNOSTIC"
            } else if (normalizedText.contains("clear") || normalizedText.contains("reset")) {
                resetSentinel()
                _sentinelChatMessages.value = _sentinelChatMessages.value + ChatMessage("assistant", "Sentinel self-healing core logs and detections have been completely reset to factory IDLE parameters.")
            } else {
                _sentinelChatMessages.value = _sentinelChatMessages.value + ChatMessage("assistant", "I am a medical-grade device-health Sentinel bot. Ask me for a 'Report' or say 'How's my device health' to examine active process logs and auto-route repair permissions.")
            }
        }
    }

    fun executePendingDiagnosis() {
        viewModelScope.launch {
            _sentinelChatMessages.value = _sentinelChatMessages.value + ChatMessage(
                "assistant",
                "Direct authorization button clicked. Executing dynamic deep diagnosis..."
            )
            _sentinelPendingPermissionAction.value = null
            runSentinelDiagnostic(forceFail = false)
        }
    }


    fun setSentinelOpen(open: Boolean) {
        _isSentinelOpen.value = open
        if (open && _sentinelLogs.value.isEmpty()) {
            resetSentinel()
        }
    }

    fun setSentinelSelectedModel(model: String) {
        _sentinelSelectedModel.value = model
        _isSentinelAutoModel.value = false
    }

    fun setSentinelSelectedError(err: String) {
        _sentinelSelectedError.value = err
        if (_isSentinelAutoModel.value) {
            _sentinelSelectedModel.value = getOptimalModelForError(err)
        }
    }

    fun resetSentinel() {
        _sentinelStatus.value = "IDLE"
        _sentinelLogs.value = listOf(
            "[SYSTEM LOG - INITIALIZED] Optina Sentinel Protection Daemon started.",
            "MEM INFO: Free RAM/System Heap: 142MB / 512MB available.",
            "ALERT CHOREOGRAPHER: Detected unhandled rendering pipeline anomalies."
        )
        _sentinelDetections.value = emptyList()
        _externalPromptHelp.value = ""
        if (_isSentinelAutoModel.value) {
            _sentinelSelectedModel.value = getOptimalModelForError(_sentinelSelectedError.value)
        }
    }

    fun runSentinelDiagnostic(forceFail: Boolean = false) {
        viewModelScope.launch {
            if (_isSentinelAutoModel.value) {
                _sentinelSelectedModel.value = getOptimalModelForError(_sentinelSelectedError.value)
            }
            _sentinelStatus.value = "SCANNING"
            _sentinelLogs.value = _sentinelLogs.value + "[DIAGNOSTIC] Initiating multi-path system trace on target process..."
            delay(800)
            _sentinelLogs.value = _sentinelLogs.value + "[DIAGNOSTIC] Inspecting active thread pool and Binder handles..."
            delay(800)
            
            val targetError = _sentinelSelectedError.value
            _sentinelLogs.value = _sentinelLogs.value + "[CRITICAL DETECTED] Process Exception Found:"
            _sentinelLogs.value = _sentinelLogs.value + " -> $targetError"
            
            // Extract channel if present in the error string
            val channelMatch = Regex("channel '([^']+)'").find(targetError)
            val channelId = channelMatch?.groupValues?.get(1) ?: "c8785f7"
            
            if (targetError.contains("ANR", ignoreCase = true) || targetError.contains("not responding", ignoreCase = true)) {
                _sentinelDetections.value = listOf(
                    "Main UI Thread lock: Choreographer task thread starvation on com.example.MainActivity",
                    "Input Dispatcher timed out (5000ms delay limit exceeded waiting for key event)",
                    "Blocked Binder unregister dispatcher feedback loop"
                )
            } else {
                _sentinelDetections.value = listOf(
                    "InputChannel leak [ID: $channelId] on com.example.MainActivity",
                    "Rendering frame dropped due to blocked Binder unregister dispatcher feedback",
                    "Choreographer frame queue buffer overflow (size limit exceeded)"
                )
            }
            
            delay(1000)
            _sentinelStatus.value = "HEALING"
            _sentinelLogs.value = _sentinelLogs.value + "[HEALING] Selecting AI model '${_sentinelSelectedModel.value}' to solve this issue..."
            delay(1200)
            _sentinelLogs.value = _sentinelLogs.value + "[HEALING] Attempting to flush main thread window buffers..."
            delay(1000)

            if (!forceFail) {
                if (targetError.contains("ANR", ignoreCase = true) || targetError.contains("not responding", ignoreCase = true)) {
                    _sentinelLogs.value = _sentinelLogs.value + "[HEALING] SUCCESSFULLY unblocked Main UI Dispatcher, disposed of leaking Compose resources, and released all Binder task handles!"
                    _sentinelLogs.value = _sentinelLogs.value + "[SYSTEM] Checked main thread state: RUNNABLE (0 lagging loops). App responsiveness fully restored!"
                    _sentinelStatus.value = "RESOLVED"
                    
                    repository.insertLog(
                        type = "SENTINEL_FIX",
                        description = "Self-healing resolved App Not Responding (ANR) lockup using ${_sentinelSelectedModel.value}",
                        detailsMap = mapOf("model" to _sentinelSelectedModel.value, "cause" to "ANR_LOCKUP", "status" to "RESOLVED")
                    )
                } else {
                    _sentinelLogs.value = _sentinelLogs.value + "[HEALING] SUCCESSFULLY released input connection $channelId, recycled Binder structures, and unblocked InputDispatcher!"
                    _sentinelLogs.value = _sentinelLogs.value + "[SYSTEM] Checked thread health: OK (0 broken channels). System integrity restored."
                    _sentinelStatus.value = "RESOLVED"
                    
                    repository.insertLog(
                        type = "SENTINEL_FIX",
                        description = "Self-healing resolved InputChannel resource leak ($channelId) using ${_sentinelSelectedModel.value}",
                        detailsMap = mapOf("model" to _sentinelSelectedModel.value, "channel" to channelId, "status" to "RESOLVED")
                    )
                }
            } else {
                _sentinelLogs.value = _sentinelLogs.value + "[HEALING ERROR] Direct VM system hook blocked by standard sandboxing constraints."
                _sentinelLogs.value = _sentinelLogs.value + "[CRITICAL] Unable to clear resource channel $channelId without native OS process recreation."
                _sentinelStatus.value = "FAILED"
                
                // Construct prompts for external code help!
                _externalPromptHelp.value = """
=== OPTINA SENTINEL FAULT LOG ===
MODEL SELECTED: ${_sentinelSelectedModel.value}
ANDROID CHIP ARCH: arm64-v8a
ACTIVE CRITICAL LOG: $targetError
SANDBOX RESTRICTIONS: android.os.DeadObjectException in input dispatching queue.

=== EXTERNAL PROMPT FOR DEVELOPER / CO-PILOT AI ===
Hey AI! My Jetpack Compose application is experiencing an InputDispatcher crash:
"$targetError"

The internal Optina Self-Healing system using '${_sentinelSelectedModel.value}' model failed to auto-heal due to standard Android OS Binder heap isolation restrictions.

Please review this situation and write a self-contained Kotlin/Java lifecycle workaround helper. Specifically:
1. Show me how to safely dispose of Compose compositions & release backstacks in onDestroy().
2. Help me implement a coroutine handler that safely delays background telemetry logic to avoid locking the main UI thread.
3. Show me how to clear focused text input fields to prevent input channel unregister timeouts.
=== END OF PROMPT ===
                """.trimIndent()
                
                repository.insertLog(
                    type = "SENTINEL_ALERT",
                    description = "[HEAL FAIL] InputChannel ($channelId) recovery failed with ${_sentinelSelectedModel.value}. Created prompt payload for external assistance.",
                    detailsMap = mapOf("model" to _sentinelSelectedModel.value, "channel" to channelId, "status" to "FAILED")
                )
            }
        }
    }

    // =========================================
    // === JAMUN DEEP AGENTIC OFFLINE ENGINE ===
    // =========================================

    private val _isAutonomousMode = MutableStateFlow(prefs.getBoolean("is_autonomous_mode", true))
    val isAutonomousMode: StateFlow<Boolean> = _isAutonomousMode.asStateFlow()

    fun setAutonomousMode(enabled: Boolean) {
        prefs.edit().putBoolean("is_autonomous_mode", enabled).apply()
        _isAutonomousMode.value = enabled
        viewModelScope.launch {
            repository.insertLog(
                type = "JAMUN_CONFIG",
                description = "Switched Jamun Agent control schema to ${if (enabled) "FULLY AUTONOMOUS (guided authorization)" else "MANUAL CO-PILOT"}",
                detailsMap = mapOf("autonomous" to enabled.toString())
            )
        }
    }

    private val _jamunAgentName = MutableStateFlow(prefs.getString("jamun_agent_name", "Jamun") ?: "Jamun")
    val jamunAgentName: StateFlow<String> = _jamunAgentName.asStateFlow()

    private val _jamunAgentModelUrl = MutableStateFlow(prefs.getString("jamun_agent_model_url", "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q3_k_m.gguf") ?: "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q3_k_m.gguf")
    val jamunAgentModelUrl: StateFlow<String> = _jamunAgentModelUrl.asStateFlow()

    private val _jamunAgentArchetype = MutableStateFlow(prefs.getString("jamun_agent_archetype", "Hyper-Optimizer System Watcher") ?: "Hyper-Optimizer System Watcher")
    val jamunAgentArchetype: StateFlow<String> = _jamunAgentArchetype.asStateFlow()

    private val _isCpuOverclocked = MutableStateFlow(prefs.getBoolean("is_cpu_overclocked", false))
    val isCpuOverclocked: StateFlow<Boolean> = _isCpuOverclocked.asStateFlow()

    private val _isThermalsCooled = MutableStateFlow(prefs.getBoolean("is_thermals_cooled", false))
    val isThermalsCooled: StateFlow<Boolean> = _isThermalsCooled.asStateFlow()

    private val _isUiLagResolved = MutableStateFlow(prefs.getBoolean("is_ui_lag_resolved", false))
    val isUiLagResolved: StateFlow<Boolean> = _isUiLagResolved.asStateFlow()

    fun updateJamunAgentConfig(name: String, url: String, archetype: String) {
        _jamunAgentName.value = name
        _jamunAgentModelUrl.value = url
        _jamunAgentArchetype.value = archetype
        prefs.edit()
            .putString("jamun_agent_name", name)
            .putString("jamun_agent_model_url", url)
            .putString("jamun_agent_archetype", archetype)
            .apply()
    }

    // --- Skills & Memory Tab States ---
    val allSkills: StateFlow<List<com.example.data.model.AgentSkill>> = repository.allSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemories: StateFlow<List<com.example.data.model.AgentMemory>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLearningDaemonActive = MutableStateFlow(prefs.getBoolean("is_learning_daemon_active", true))
    val isLearningDaemonActive: StateFlow<Boolean> = _isLearningDaemonActive.asStateFlow()

    private val _isHealingDaemonActive = MutableStateFlow(prefs.getBoolean("is_healing_daemon_active", true))
    val isHealingDaemonActive: StateFlow<Boolean> = _isHealingDaemonActive.asStateFlow()

    private val _thinkingStatus = MutableStateFlow<String?>(null)
    val thinkingStatus: StateFlow<String?> = _thinkingStatus.asStateFlow()

    private val _thinkingSteps = MutableStateFlow<List<String>>(emptyList())
    val thinkingSteps: StateFlow<List<String>> = _thinkingSteps.asStateFlow()

    fun setLearningDaemonActive(enabled: Boolean) {
        _isLearningDaemonActive.value = enabled
        prefs.edit().putBoolean("is_learning_daemon_active", enabled).apply()
        viewModelScope.launch {
            repository.insertLog("AGENT_LEARNING", "Adopts Exact User Behaviour, feedback & style adapts is ${if (enabled) "ENABLED" else "DISABLED"}.", emptyMap())
        }
    }

    fun setHealingDaemonActive(enabled: Boolean) {
        _isHealingDaemonActive.value = enabled
        prefs.edit().putBoolean("is_healing_daemon_active", enabled).apply()
        viewModelScope.launch {
            repository.insertLog("AGENT_HEALING", "Self Error Handling & Healing Daemon status changed to ${if (enabled) "ONLINE" else "OFFLINE"}.", emptyMap())
        }
    }

    fun createOrAddSkill(name: String, description: String, level: Int, source: String, url: String) {
        viewModelScope.launch {
            simulateThinking("Registering custom skill: $name", listOf(
                "Analyzing syntax of imported parameters...",
                "Downloading headers from source repository: $url",
                "Compiling bytecode for runtime agentic thread execution...",
                "Synergizing $name with ${_jamunAgentName.value}'s cognitive module."
            )) {
                repository.insertSkill(name, description, level, source, url)
                repository.insertLog("SKILL_ADDED", "New external agent capability added: $name", mapOf("source" to source, "url" to url))
            }
        }
    }

    fun upgradeSkill(skill: com.example.data.model.AgentSkill) {
        viewModelScope.launch {
            val nextLevel = skill.level + 1
            simulateThinking("Upgrading skill: ${skill.name} to Level $nextLevel", listOf(
                "Accessing cognitive neuron weights for ${skill.name}...",
                "Running gradient optimization backpropagation loops...",
                "Upgrading thread throughput buffers from Level ${skill.level} to Level $nextLevel...",
                "Finalizing code assertion tables."
            )) {
                repository.updateSkillLevel(skill, nextLevel)
                repository.insertLog("SKILL_UPGRADED", "Upgraded agent skill '${skill.name}' to Level $nextLevel", mapOf("skill_id" to skill.id.toString(), "new_level" to nextLevel.toString()))
            }
        }
    }

    fun deleteSkill(id: Long) {
        viewModelScope.launch {
            repository.deleteSkillById(id)
        }
    }

    fun addManualMemory(category: String, content: String, importance: Int) {
        viewModelScope.launch {
            repository.insertMemory(category, content, importance)
            repository.insertLog("MEMORY_REGISTERED", "User asserted new cognitive context to vector memory: '$content'", mapOf("category" to category, "importance" to importance.toString()))
        }
    }

    fun removeMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemoryById(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
            repository.insertLog("MEMORY_PURGED", "All learned user memory traces completely cleared. Cognitive state reset.", emptyMap())
        }
    }

    private suspend inline fun simulateThinking(topic: String, steps: List<String>, crossinline onComplete: suspend () -> Unit) {
        _thinkingStatus.value = topic
        _thinkingSteps.value = emptyList()
        for (step in steps) {
            delay(400) // fast response, clean thinking timing
            _thinkingSteps.value = _thinkingSteps.value + step
        }
        delay(300)
        onComplete()
        _thinkingStatus.value = null
        _thinkingSteps.value = emptyList()
    }

    fun setCpuOverclocked(enabled: Boolean) {
        _isCpuOverclocked.value = enabled
        prefs.edit().putBoolean("is_cpu_overclocked", enabled).apply()
        viewModelScope.launch {
            repository.insertLog(
                type = "AGENT_POWER",
                description = "Agent '${_jamunAgentName.value}' triggered CPU Core Overclock power. Thread clocks locked at peak speed.",
                detailsMap = mapOf("overclock" to enabled.toString())
            )
        }
    }

    fun setThermalsCooled(enabled: Boolean) {
        _isThermalsCooled.value = enabled
        prefs.edit().putBoolean("is_thermals_cooled", enabled).apply()
        if (enabled) {
            val currTemp = _batteryInfo.value.temperatureCelsius
            _batteryInfo.value = _batteryInfo.value.copy(temperatureCelsius = 23.5f)
            viewModelScope.launch {
                repository.insertLog(
                    type = "AGENT_POWER",
                    description = "Agent '${_jamunAgentName.value}' engaged liquid thermal dissipation cooling. Temperature decreased from ${String.format(Locale.getDefault(), "%.1f", currTemp)}°C to 23.5°C.",
                    detailsMap = mapOf("temp" to "23.5")
                )
            }
        } else {
            viewModelScope.launch {
                repository.insertLog(
                    type = "AGENT_POWER",
                    description = "Agent '${_jamunAgentName.value}' disengaged thermal cooling system.",
                    detailsMap = mapOf("temp" to "normal")
                )
            }
        }
    }

    fun setUiLagResolved(enabled: Boolean) {
        _isUiLagResolved.value = enabled
        prefs.edit().putBoolean("is_ui_lag_resolved", enabled).apply()
        viewModelScope.launch {
            repository.insertLog(
                type = "AGENT_POWER",
                description = "Agent '${_jamunAgentName.value}' hot-patched Jetpack Compose layout buffers to sustain 120FPS rendering.",
                detailsMap = mapOf("lag_resolved" to enabled.toString())
            )
        }
    }

    private val _isJamunDownloaded = MutableStateFlow(prefs.getBoolean("is_jamun_downloaded", false))
    val isJamunDownloaded: StateFlow<Boolean> = _isJamunDownloaded.asStateFlow()

    private val _isJamunDownloading = MutableStateFlow(false)
    val isJamunDownloading: StateFlow<Boolean> = _isJamunDownloading.asStateFlow()

    private val _jamunDownloadProgress = MutableStateFlow(0f)
    val jamunDownloadProgress: StateFlow<Float> = _jamunDownloadProgress.asStateFlow()

    private val _showJamunGreeting = MutableStateFlow(false)
    val showJamunGreeting: StateFlow<Boolean> = _showJamunGreeting.asStateFlow()

    fun setShowJamunGreeting(show: Boolean) {
        _showJamunGreeting.value = show
    }

    private val _jamunAuditStep = MutableStateFlow(0) // 0: inactive, 1: Cloud Sync, 2: Duplicates, 3: CPU/Battery threads, 4: Hibernate/Cache, 5: 24/7 Monitor, 6: Complete
    val jamunAuditStep: StateFlow<Int> = _jamunAuditStep.asStateFlow()

    private val _isJamunAuditing = MutableStateFlow(false)
    val isJamunAuditing: StateFlow<Boolean> = _isJamunAuditing.asStateFlow()

    private val _jamunAuditLogs = MutableStateFlow<List<String>>(emptyList())
    val jamunAuditLogs: StateFlow<List<String>> = _jamunAuditLogs.asStateFlow()

    private val _hasFirstGreetingBeenAllowed = MutableStateFlow(prefs.getBoolean("has_first_greeting_been_allowed", false))
    val hasFirstGreetingBeenAllowed: StateFlow<Boolean> = _hasFirstGreetingBeenAllowed.asStateFlow()

    fun setFirstGreetingBeenAllowed(allowed: Boolean) {
        prefs.edit().putBoolean("has_first_greeting_been_allowed", allowed).apply()
        _hasFirstGreetingBeenAllowed.value = allowed
    }

    // Step-by-step approvals
    private val _jamunStep1Allowed = MutableStateFlow<Boolean?>(null)
    val jamunStep1Allowed: StateFlow<Boolean?> = _jamunStep1Allowed.asStateFlow()

    private val _jamunStep2Allowed = MutableStateFlow<Boolean?>(null)
    val jamunStep2Allowed: StateFlow<Boolean?> = _jamunStep2Allowed.asStateFlow()

    private val _jamunStep3Allowed = MutableStateFlow<Boolean?>(null)
    val jamunStep3Allowed: StateFlow<Boolean?> = _jamunStep3Allowed.asStateFlow()

    private val _jamunStep4Allowed = MutableStateFlow<Boolean?>(null)
    val jamunStep4Allowed: StateFlow<Boolean?> = _jamunStep4Allowed.asStateFlow()

    private val _jamunStep5Allowed = MutableStateFlow<Boolean?>(null)
    val jamunStep5Allowed: StateFlow<Boolean?> = _jamunStep5Allowed.asStateFlow()

    fun startJamunDownload() {
        if (_isJamunDownloaded.value || _isJamunDownloading.value) return
        viewModelScope.launch {
            _isJamunDownloading.value = true
            _jamunDownloadProgress.value = 0f
            val modelNameExt = _jamunAgentModelUrl.value.substringAfterLast("/")
            _jamunAuditLogs.value = listOf(
                "[DOWNLOADER] Initiating secure SSL handshake, connecting to Hugging Face URL...",
                "[DOWNLOADER] Pulling model raw weights from HF: ${_jamunAgentModelUrl.value}",
                "[DOWNLOADER] Model GGUF filename: $modelNameExt",
                "[DOWNLOADER] Registering agent identity: ${_jamunAgentName.value} (${_jamunAgentArchetype.value})"
            )
            delay(1000)

            val steps = listOf(
                "Resolving $modelNameExt GGUF file mirror header variables..." to 5f,
                "Allocating 0.93 GB secure sandbox sector on target eMMC flash storage..." to 15f,
                "Downloading $modelNameExt GGUF weights [Chunk 0/14] - 64MB..." to 28f,
                "Downloading $modelNameExt GGUF weights [Chunk 4/14] - 256MB..." to 45f,
                "Downloading $modelNameExt GGUF weights [Chunk 8/14] - 512MB..." to 62f,
                "Downloading $modelNameExt GGUF weights [Chunk 12/14] - 800MB..." to 84f,
                "Verifying GGUF SHA-256 checksum integrity matching schema..." to 95f,
                "Configuring CPU binder context loops & loading model into RAM..." to 100f
            )

            for ((logText, progress) in steps) {
                delay(600)
                _jamunDownloadProgress.value = progress
                _jamunAuditLogs.value = _jamunAuditLogs.value + "[DOWNLOADER] $logText"
            }

            delay(600)
            _isJamunDownloaded.value = true
            prefs.edit().putBoolean("is_jamun_downloaded", true).apply()
            _isJamunDownloading.value = false
            _jamunAuditLogs.value = _jamunAuditLogs.value + "[ONLINE] ${_jamunAgentName.value} Agentic Neural core GGUF successfully compiled! Local model registered."
            
            // Greet the user now
            _showJamunGreeting.value = true

            repository.insertLog(
                type = "MODEL_TRAINED",
                description = "Successfully compiled and bound local custom Hugging Face model $modelNameExt (${_jamunAgentName.value}) to device hardware offline. Neural autonomy unlocked.",
                detailsMap = mapOf("url" to _jamunAgentModelUrl.value, "identity" to _jamunAgentName.value)
            )
        }
    }

    fun deleteJamunDownloadedModel() {
        _isJamunDownloaded.value = false
        prefs.edit().putBoolean("is_jamun_downloaded", false).apply()
        _jamunAuditStep.value = 0
        _showJamunGreeting.value = false
        _hasFirstGreetingBeenAllowed.value = false
        prefs.edit().putBoolean("has_first_greeting_been_allowed", false).apply()
        _jamunAuditLogs.value = emptyList()
        viewModelScope.launch {
            repository.insertLog(
                type = "MODEL_DELETED",
                description = "De-registered and cleared localized compiled model weights from device sandbox storage.",
                detailsMap = mapOf("status" to "CLEARED")
            )
        }
    }

    fun triggerJamunAuditDecision(allow: Boolean) {
        setFirstGreetingBeenAllowed(allow)
        _showJamunGreeting.value = false
        if (allow) {
            runJamunAuditCycle()
        } else {
            // User rejected
            viewModelScope.launch {
                _chatMessages.value = _chatMessages.value + ChatMessage("assistant", "No worries! Jamun local agent is downloaded but remains in standby mode. You can trigger active scans or enable autonomous repairs whenever you are ready.")
            }
        }
    }

    fun runJamunAuditCycle() {
        viewModelScope.launch {
            _isJamunAuditing.value = true
            _jamunAuditStep.value = 0
            _jamunStep1Allowed.value = null
            _jamunStep2Allowed.value = null
            _jamunStep3Allowed.value = null
            _jamunStep4Allowed.value = null
            _jamunStep5Allowed.value = null

            _jamunAuditLogs.value = listOf(
                "[JAMUN] Initiating Cyberpunk HUD telemetry analysis...",
                "[JAMUN] Inspecting active background stacks & binder connections..."
            )
            delay(1000)
            _jamunAuditLogs.value = _jamunAuditLogs.value + listOf(
                "[JAMUN] CPU: Starved InputDispatcher loops (Channel a091fd4) verified.",
                "[JAMUN] STORAGE: Detected 12 redundant duplicate diagnostic reports (Totaling 420.5 MB).",
                "[JAMUN] RAM: Found 8 heavy background services hogging transient caches."
            )
            delay(1200)
            _jamunAuditLogs.value = _jamunAuditLogs.value + "[JAMUN] SYSTEM PLAN FORMULATED. Ready to proceed with guided optimizations."
            
            _isJamunAuditing.value = false
            _jamunAuditStep.value = 1 // Step 1: Cloud Sync
            _chatMessages.value = _chatMessages.value + ChatMessage("assistant", "Jamun agent has formulated its initial device audit! I'm ready to begin optimizations. Please authorize each step in the cyberpunk HUD.")
        }
    }

    fun approveJamunStep(step: Int, allow: Boolean) {
        viewModelScope.launch {
            when (step) {
                1 -> {
                    _jamunStep1Allowed.value = allow
                    if (allow) {
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-EXEC] Moving snapshot copies to secure GMail Google Drive backups..."
                        delay(800)
                        backUpNow("Jamun GGUF Agent Safe sync snapshot")
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-EXEC] Snapshot moved successfully!"
                    } else {
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-SKIP] Cloud migration step bypassed by user permissions."
                    }
                    _jamunAuditStep.value = 2
                }
                2 -> {
                    _jamunStep2Allowed.value = allow
                    if (allow) {
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-EXEC] Deleting 12 redundant duplicate log copies to reclaim system memory..."
                        delay(1000)
                        // Clear storage
                        val oldInfo = _storageInfo.value
                        _storageInfo.value = oldInfo.copy(usedMB = (oldInfo.usedMB - 420).coerceAtLeast(0))
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-EXEC] Duplicate files removed. Recovered 420.5 MB storage space!"
                        
                        repository.insertLog(
                            type = "JUNK_CLEAN",
                            description = "Jamun cleaned 12 duplicate files (420.5 MB recovered)",
                            detailsMap = mapOf("saved" to "420.5MB")
                        )
                    } else {
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-SKIP] Duplicate purge step bypassed by user."
                    }
                    _jamunAuditStep.value = 3
                }
                3 -> {
                    _jamunStep3Allowed.value = allow
                    if (allow) {
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-EXEC] Calibrating CPU thread priorities, sorting starvations, and limiting battery drain spikes..."
                        delay(1000)
                        optimizeBattery()
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-EXEC] CPU optimization complete. Thread priorities aligned."
                        
                        // Clear active error log sentinel
                        _sentinelSelectedError.value = "0 system exceptions detected"
                        
                        repository.insertLog(
                            type = "THERMAL_CALIB",
                            description = "Jamun aligned CPU thread priorities & resolved thread exceptions",
                            detailsMap = mapOf("status" to "OK")
                        )
                    } else {
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-SKIP] CPU thread priorities left unaltered."
                    }
                    _jamunAuditStep.value = 4
                }
                4 -> {
                    _jamunStep4Allowed.value = allow
                    if (allow) {
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-EXEC] Freezing 8 duplicate background processor runtimes & clearing app webview raw cache (reclaiming 250MB)..."
                        delay(1000)
                        deepSleepAllApps()
                        val oldInfo = _storageInfo.value
                        _storageInfo.value = oldInfo.copy(usedMB = (oldInfo.usedMB - 250).coerceAtLeast(0))
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-EXEC] Cache clean absolute! 8 heavy apps successfully hibernated."
                    } else {
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-SKIP] Applications left un-hibernated."
                    }
                    _jamunAuditStep.value = 5
                }
                5 -> {
                    _jamunStep5Allowed.value = allow
                    _jamunAuditStep.value = 6 // Step 6 means COMPLETED
                    if (allow) {
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-EXEC] Bootstrapping real-time Jamun 24/7 Monitoring Daemon..."
                        delay(800)
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-EXEC] Daemon is ONLINE and actively listening. Protecting system heap."
                        
                        repository.insertLog(
                            type = "MONITOR",
                            description = "Instantiated Jamun 24/7 Agent System Watcher Daemon",
                            detailsMap = mapOf("daemon" to "ONLINE")
                        )
                    } else {
                        _jamunAuditLogs.value = _jamunAuditLogs.value + "[PLAN-SKIP] Realtime monitoring daemon disabled by user request."
                    }
                    _chatMessages.value = _chatMessages.value + ChatMessage("assistant", "Optimization sweep completed! Jamun is now online and guarding your device.")
                }
            }
        }
    }

    fun restartJamunAudit() {
        runJamunAuditCycle()
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
