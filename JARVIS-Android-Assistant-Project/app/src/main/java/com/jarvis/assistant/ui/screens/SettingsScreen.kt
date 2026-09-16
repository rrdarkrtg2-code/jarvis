package com.jarvis.assistant.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.JarvisApp
import com.jarvis.assistant.core.Constants
import com.jarvis.assistant.services.FloatingBubbleService
import com.jarvis.assistant.services.JarvisBackgroundService
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisError
import com.jarvis.assistant.ui.theme.JarvisSuccess
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateToDiagnostics: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as JarvisApp
    val securityMgr = app.securityManager
    val settingsRepo = app.settingsRepository
    val scope = rememberCoroutineScope()

    val providers = listOf(
        Constants.PROVIDER_GEMINI,
        Constants.PROVIDER_OPENAI,
        Constants.PROVIDER_OPENROUTER,
        Constants.PROVIDER_LOCAL_LAN
    )

    var selectedProvider by remember { mutableStateOf(Constants.PROVIDER_GEMINI) }
    var apiKey by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var providerDropdownExpanded by remember { mutableStateOf(false) }

    var speechRate by remember { mutableFloatStateOf(1.0f) }
    var pitch by remember { mutableFloatStateOf(1.0f) }

    var backgroundServiceEnabled by remember { mutableStateOf(false) }
    var floatingBubbleEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        selectedProvider = settingsRepo.getString(Constants.KEY_AI_PROVIDER, Constants.PROVIDER_GEMINI)
        apiKey = securityMgr.getApiKey(selectedProvider)
        speechRate = settingsRepo.getFloat(Constants.KEY_VOICE_SPEECH_RATE, 1.0f)
        pitch = settingsRepo.getFloat(Constants.KEY_VOICE_PITCH, 1.0f)
        backgroundServiceEnabled = settingsRepo.getBoolean(Constants.KEY_BACKGROUND_SERVICE, false)
        floatingBubbleEnabled = settingsRepo.getBoolean(Constants.KEY_FLOATING_BUBBLE, false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "SYSTEM SETTINGS",
            color = JarvisCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Configure intelligence, voice synthesis, security, and permissions",
            color = JarvisTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: AI PROVIDER CONFIG
        SettingsSectionCard(title = "AI INTELLIGENCE PROVIDER") {
            Text("Select Provider", color = JarvisTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = providerDropdownExpanded,
                onExpandedChange = { providerDropdownExpanded = !providerDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedProvider,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisCardBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = providerDropdownExpanded,
                    onDismissRequest = { providerDropdownExpanded = false }
                ) {
                    providers.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider) },
                            onClick = {
                                selectedProvider = provider
                                providerDropdownExpanded = false
                                scope.launch {
                                    settingsRepo.setString(Constants.KEY_AI_PROVIDER, provider)
                                    apiKey = securityMgr.getApiKey(provider)
                                    testResult = null
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("API Key (Stored with Hardware-Backed Encryption)", color = JarvisTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                placeholder = { Text("Paste $selectedProvider API Key...", color = JarvisTextSecondary) },
                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisCardBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        securityMgr.saveApiKey(selectedProvider, apiKey)
                        testResult = "Key securely saved."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Key", color = JarvisBackground, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        securityMgr.saveApiKey(selectedProvider, apiKey)
                        isTesting = true
                        scope.launch {
                            val provider = app.getActiveAIProvider()
                            val ok = provider?.testConnection() ?: false
                            isTesting = false
                            testResult = if (ok) "Connection Successful!" else "Connection Failed. Check key/network."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isTesting) "Testing..." else "Test Connection")
                }
            }

            testResult?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    color = if (msg.contains("Successful") || msg.contains("saved")) JarvisSuccess else JarvisError,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: VOICE SYNTHESIS
        SettingsSectionCard(title = "VOICE ENGINE") {
            Text("Speech Rate: ${String.format("%.2f", speechRate)}x", color = JarvisTextPrimary, fontSize = 13.sp)
            Slider(
                value = speechRate,
                onValueChange = {
                    speechRate = it
                    scope.launch { settingsRepo.setFloat(Constants.KEY_VOICE_SPEECH_RATE, it) }
                },
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Pitch: ${String.format("%.2f", pitch)}x", color = JarvisTextPrimary, fontSize = 13.sp)
            Slider(
                value = pitch,
                onValueChange = {
                    pitch = it
                    scope.launch { settingsRepo.setFloat(Constants.KEY_VOICE_PITCH, it) }
                },
                valueRange = 0.5f..1.5f,
                colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { app.voiceEngine.speak("Voice test sequence completed successfully.", speechRate, pitch) },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Test Voice Synthesis")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: AUTOMATION & BACKGROUND
        SettingsSectionCard(title = "AUTOMATION & SERVICES") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Background Assistant Service", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Maintains persistent standby notification", color = JarvisTextSecondary, fontSize = 11.sp)
                }
                Switch(
                    checked = backgroundServiceEnabled,
                    onCheckedChange = { enable ->
                        backgroundServiceEnabled = enable
                        scope.launch { settingsRepo.setBoolean(Constants.KEY_BACKGROUND_SERVICE, enable) }
                        val intent = Intent(context, JarvisBackgroundService::class.java)
                        if (enable) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        } else {
                            context.stopService(intent)
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisCardBorder)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Floating J.A.R.V.I.S. Bubble", color = JarvisTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Overlay accessible above all applications", color = JarvisTextSecondary, fontSize = 11.sp)
                }
                Switch(
                    checked = floatingBubbleEnabled,
                    onCheckedChange = { enable ->
                        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } else {
                            floatingBubbleEnabled = enable
                            scope.launch { settingsRepo.setBoolean(Constants.KEY_FLOATING_BUBBLE, enable) }
                            val intent = Intent(context, FloatingBubbleService::class.java)
                            if (enable) context.startService(intent) else context.stopService(intent)
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisCardBorder)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: SYSTEM DIAGNOSTICS & PERMISSIONS
        SettingsSectionCard(title = "DIAGNOSTICS & RESET") {
            Button(
                onClick = onNavigateToDiagnostics,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Open Developer Diagnostics Console")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    scope.launch {
                        app.database.memoryDao().clearAll()
                        app.database.conversationDao().clearAll()
                        app.database.reminderDao().clearAll()
                        app.database.auditLogDao().clearLogs()
                        securityMgr.clearAllSecureData()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisError),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reset All Local Data & Secrets", color = JarvisTextPrimary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisCard)
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = JarvisCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
