package com.jarvis.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.jarvis.assistant.ai.AIContextBuilder
import com.jarvis.assistant.ai.AIProvider
import com.jarvis.assistant.ai.GeminiProvider
import com.jarvis.assistant.ai.LocalLanProvider
import com.jarvis.assistant.ai.OpenAIProvider
import com.jarvis.assistant.ai.OpenRouterProvider
import com.jarvis.assistant.automation.AppDiscoveryManager
import com.jarvis.assistant.automation.DeviceController
import com.jarvis.assistant.core.Constants
import com.jarvis.assistant.core.SecurityManager
import com.jarvis.assistant.data.local.JarvisDatabase
import com.jarvis.assistant.data.repository.AuditRepository
import com.jarvis.assistant.data.repository.ConversationRepository
import com.jarvis.assistant.data.repository.MemoryRepository
import com.jarvis.assistant.data.repository.ReminderRepository
import com.jarvis.assistant.data.repository.SettingsRepository
import com.jarvis.assistant.engine.ConfirmationSystem
import com.jarvis.assistant.engine.IntentRouter
import com.jarvis.assistant.engine.LocalCommandEngine
import com.jarvis.assistant.reminders.ReminderManager
import com.jarvis.assistant.voice.VoiceEngine
import kotlinx.coroutines.runBlocking

class JarvisApp : Application() {

    lateinit var database: JarvisDatabase
        private set
    lateinit var securityManager: SecurityManager
        private set
    lateinit var memoryRepository: MemoryRepository
        private set
    lateinit var reminderRepository: ReminderRepository
        private set
    lateinit var conversationRepository: ConversationRepository
        private set
    lateinit var auditRepository: AuditRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var deviceController: DeviceController
        private set
    lateinit var appDiscoveryManager: AppDiscoveryManager
        private set
    lateinit var reminderManager: ReminderManager
        private set
    lateinit var localCommandEngine: LocalCommandEngine
        private set
    lateinit var confirmationSystem: ConfirmationSystem
        private set
    lateinit var aiContextBuilder: AIContextBuilder
        private set
    lateinit var intentRouter: IntentRouter
        private set
    lateinit var voiceEngine: VoiceEngine
        private set

    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()

        database = JarvisDatabase.getInstance(this)
        securityManager = SecurityManager(this)

        memoryRepository = MemoryRepository(database.memoryDao())
        reminderRepository = ReminderRepository(database.reminderDao())
        conversationRepository = ConversationRepository(database.conversationDao())
        auditRepository = AuditRepository(database.auditLogDao())
        settingsRepository = SettingsRepository(database.preferenceDao())

        deviceController = DeviceController(this)
        appDiscoveryManager = AppDiscoveryManager(this)
        reminderManager = ReminderManager(this, reminderRepository)
        localCommandEngine = LocalCommandEngine()
        confirmationSystem = ConfirmationSystem()

        aiContextBuilder = AIContextBuilder(
            memoryRepository = memoryRepository,
            conversationRepository = conversationRepository,
            deviceController = deviceController
        )

        intentRouter = IntentRouter(
            localCommandEngine = localCommandEngine,
            deviceController = deviceController,
            appDiscoveryManager = appDiscoveryManager,
            memoryRepository = memoryRepository,
            reminderManager = reminderManager,
            conversationRepository = conversationRepository,
            auditRepository = auditRepository,
            aiContextBuilder = aiContextBuilder,
            confirmationSystem = confirmationSystem,
            aiProviderSelector = { getActiveAIProvider() }
        )

        voiceEngine = VoiceEngine(
            context = this,
            onSpeechRecognized = { text ->
                // Broadcast or handle in ViewModel
            },
            onError = { err ->
                // Voice error handling
            },
            onAudioLevel = { level ->
                // Visualizer level
            }
        )
    }

    fun getActiveAIProvider(): AIProvider? {
        val providerName = runBlocking { settingsRepository.getString(Constants.KEY_AI_PROVIDER, Constants.PROVIDER_GEMINI) }
        val model = runBlocking { settingsRepository.getString(Constants.KEY_AI_MODEL, "") }

        return when (providerName) {
            Constants.PROVIDER_GEMINI -> GeminiProvider(
                apiKeyProvider = { securityManager.getApiKey(Constants.PROVIDER_GEMINI) },
                model = model.ifEmpty { Constants.DEFAULT_GEMINI_MODEL }
            )
            Constants.PROVIDER_OPENAI -> OpenAIProvider(
                apiKeyProvider = { securityManager.getApiKey(Constants.PROVIDER_OPENAI) },
                model = model.ifEmpty { Constants.DEFAULT_OPENAI_MODEL }
            )
            Constants.PROVIDER_OPENROUTER -> OpenRouterProvider(
                apiKeyProvider = { securityManager.getApiKey(Constants.PROVIDER_OPENROUTER) },
                model = model.ifEmpty { Constants.DEFAULT_OPENROUTER_MODEL }
            )
            Constants.PROVIDER_LOCAL_LAN -> LocalLanProvider(
                endpointUrl = { runBlocking { settingsRepository.getString(Constants.KEY_CUSTOM_ENDPOINT, Constants.DEFAULT_LOCAL_ENDPOINT) } },
                modelName = { model.ifEmpty { "llama3" } }
            )
            else -> null
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val serviceChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SERVICE,
                "J.A.R.V.I.S. Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent status while assistant is running"
            }

            val remindersChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_REMINDERS,
                "J.A.R.V.I.S. Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task reminders and alarm notifications"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(remindersChannel)
        }
    }
}
