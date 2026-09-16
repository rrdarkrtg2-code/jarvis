package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ui.components.JarvisCoreOrb
import com.jarvis.assistant.ui.components.OrbState
import com.jarvis.assistant.ui.components.SystemStatusCard
import com.jarvis.assistant.ui.components.WaveformVisualizer
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisBlue
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisCyanBright
import com.jarvis.assistant.ui.theme.JarvisError
import com.jarvis.assistant.ui.theme.JarvisSuccess
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import com.jarvis.assistant.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "J.A.R.V.I.S.",
                    color = JarvisCyan,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Always Here For You",
                    color = JarvisTextSecondary,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = JarvisCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Greeting Card
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
                    text = state.greetingMessage,
                    color = JarvisTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (state.lastResponse.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.lastResponse,
                        color = JarvisCyanBright,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Central Animated Core Orb
        JarvisCoreOrb(
            state = state.orbState,
            audioLevel = state.audioLevel,
            size = 210.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live Audio Reactive Waveform
        WaveformVisualizer(
            isActive = state.orbState == OrbState.LISTENING || state.orbState == OrbState.SPEAKING,
            audioLevel = state.audioLevel
        )

        // Status pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(JarvisCard)
                .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = state.statusMessage,
                color = JarvisCyanBright,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Pending Confirmation Prompt (if any)
        state.pendingConfirmation?.let { req ->
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisCard)
                    .border(1.dp, JarvisError, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "CONFIRMATION REQUIRED: ${req.actionName}",
                        color = JarvisError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = req.description, color = JarvisTextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { viewModel.cancelPendingAction() },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBorder)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.confirmPendingAction() },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                        ) {
                            Text("Confirm", color = JarvisBackground)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Feature Cards (matching screenshot)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickFeatureCard(
                title = "Voice Assistant",
                subtitle = "Talk to JARVIS",
                onClick = { viewModel.onMicrophoneClicked() },
                modifier = Modifier.weight(1f)
            )
            QuickFeatureCard(
                title = "Apps & Tools",
                subtitle = "Device Controls",
                onClick = onNavigateToTools,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Text input bar with microphone button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask J.A.R.V.I.S. anything...", color = JarvisTextSecondary) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisCardBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    focusedContainerColor = JarvisCard,
                    unfocusedContainerColor = JarvisCard
                ),
                trailingIcon = {
                    if (textInput.isNotEmpty()) {
                        IconButton(onClick = {
                            val q = textInput
                            textInput = ""
                            viewModel.processQuery(q)
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = JarvisCyan)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (state.isMicListening) JarvisCyanBright else JarvisCard)
                    .border(2.dp, JarvisCyan, CircleShape)
                    .clickable { viewModel.onMicrophoneClicked() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (state.isMicListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = if (state.isMicListening) JarvisBackground else JarvisCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // System Status Card
        SystemStatusCard()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun QuickFeatureCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisCard)
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Text(text = title, color = JarvisCyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = JarvisTextSecondary, fontSize = 12.sp)
        }
    }
}
