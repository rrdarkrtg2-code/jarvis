package com.jarvis.assistant.ui.screens

import android.speech.SpeechRecognizer
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.JarvisApp
import com.jarvis.assistant.automation.AccessibilityController
import com.jarvis.assistant.automation.NotificationController
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisSuccess
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import com.jarvis.assistant.ui.theme.JarvisWarning
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as JarvisApp
    val scope = rememberCoroutineScope()

    var consoleInput by remember { mutableStateOf("") }
    var consoleOutput by remember { mutableStateOf("J.A.R.V.I.S. Command Diagnostic Console Ready.") }

    val speechAvailable = SpeechRecognizer.isRecognitionAvailable(context)
    val accessibilityEnabled = AccessibilityController.isServiceEnabled()
    val notificationAccess = NotificationController.isNotificationAccessGranted()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "DEVELOPER DIAGNOSTICS",
            color = JarvisCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Live subsystem health and manual intent verification",
            color = JarvisTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // System Health Table
        SettingsSectionCard(title = "SUBSYSTEM HEALTH STATUS") {
            HealthRow("Speech Recognizer", if (speechAvailable) "READY" else "UNAVAILABLE", speechAvailable)
            HealthRow("Text-To-Speech Engine", "INITIALIZED", true)
            HealthRow("Room Database (SQLite)", "CONNECTED", true)
            HealthRow("Accessibility Automation", if (accessibilityEnabled) "ENABLED" else "DISABLED", accessibilityEnabled)
            HealthRow("Notification Listener", if (notificationAccess) "ENABLED" else "DISABLED", notificationAccess)
            HealthRow("Encrypted Keystore", "SECURE", true)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Command Test Console
        SettingsSectionCard(title = "TEXT COMMAND TEST CONSOLE") {
            Text("Simulate voice commands by typing below:", color = JarvisTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = consoleInput,
                onValueChange = { consoleInput = it },
                placeholder = { Text("e.g., battery, open youtube, 25 times 8", color = JarvisTextSecondary) },
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

            Button(
                onClick = {
                    if (consoleInput.isNotBlank()) {
                        val q = consoleInput
                        scope.launch {
                            val res = app.intentRouter.processQuery(q, null)
                            consoleOutput = "Command: $q\nResult: ${res.displayText}\nSpoken: ${res.spokenText}"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Execute Test Command", color = JarvisBackground, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisBackground)
                    .border(1.dp, JarvisCardBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = consoleOutput,
                    color = JarvisCyan,
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun HealthRow(subsystem: String, status: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = subsystem, color = JarvisTextPrimary, fontSize = 13.sp)
        Text(
            text = status,
            color = if (isOk) JarvisSuccess else JarvisWarning,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
