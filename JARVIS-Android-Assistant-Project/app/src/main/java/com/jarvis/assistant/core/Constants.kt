package com.jarvis.assistant.core

object Constants {
    const val APP_NAME = "J.A.R.V.I.S."
    const val APP_SUBTITLE = "Personal AI Operating Assistant"

    const val NOTIFICATION_CHANNEL_SERVICE = "jarvis_service_channel"
    const val NOTIFICATION_CHANNEL_REMINDERS = "jarvis_reminders_channel"
    const val NOTIFICATION_CHANNEL_ALERTS = "jarvis_alerts_channel"

    const val SERVICE_NOTIFICATION_ID = 1001
    const val REMINDER_NOTIFICATION_ID_BASE = 2000

    const val PREFS_NAME = "jarvis_secure_prefs"
    const val KEY_AI_PROVIDER = "pref_ai_provider"
    const val KEY_API_KEY = "pref_api_key"
    const val KEY_AI_MODEL = "pref_ai_model"
    const val KEY_CUSTOM_ENDPOINT = "pref_custom_endpoint"
    const val KEY_VOICE_SPEECH_RATE = "pref_voice_speech_rate"
    const val KEY_VOICE_PITCH = "pref_voice_pitch"
    const val KEY_WAKE_WORD_ENABLED = "pref_wake_word_enabled"
    const val KEY_CONTINUOUS_MODE = "pref_continuous_mode"
    const val KEY_CONFIRMATION_LEVEL = "pref_confirmation_level"
    const val KEY_FLOATING_BUBBLE = "pref_floating_bubble"
    const val KEY_BACKGROUND_SERVICE = "pref_background_service"
    const val KEY_ONBOARDING_COMPLETED = "pref_onboarding_completed"

    const val PROVIDER_GEMINI = "Google Gemini"
    const val PROVIDER_OPENAI = "OpenAI"
    const val PROVIDER_OPENROUTER = "OpenRouter"
    const val PROVIDER_LOCAL_LAN = "Local LAN / Ollama"

    const val DEFAULT_GEMINI_MODEL = "gemini-1.5-flash"
    const val DEFAULT_OPENAI_MODEL = "gpt-4o-mini"
    const val DEFAULT_OPENROUTER_MODEL = "meta-llama/llama-3.1-8b-instruct"
    const val DEFAULT_LOCAL_ENDPOINT = "http://192.168.1.100:11434/v1"

    const val ACTION_REMINDER_ALERT = "com.jarvis.assistant.ACTION_REMINDER_ALERT"
    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
}
