# JARVIS ProGuard / R8 Rules
-keep class com.jarvis.assistant.data.local.entity.** { *; }
-keep class com.jarvis.assistant.ai.models.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
