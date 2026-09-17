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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.JarvisApp
import com.jarvis.assistant.ui.components.JarvisCoreOrb
import com.jarvis.assistant.ui.components.OrbState
import com.jarvis.assistant.ui.components.WaveformVisualizer
import com.jarvis.assistant.ui.theme.JarvisAmber
import com.jarvis.assistant.ui.theme.JarvisAmberBright
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisBlue
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisError
import com.jarvis.assistant.ui.theme.JarvisMagenta
import com.jarvis.assistant.ui.theme.JarvisPurple
import com.jarvis.assistant.ui.theme.JarvisSuccess
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import com.jarvis.assistant.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenOwnerMenu: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as JarvisApp
    val state by viewModel.uiState.collectAsState()
    var textInput by remember { mutableStateOf("") }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())
    val batteryText = app.deviceController.getBatteryLevel().filter { it.isDigit() }.take(2).let { if (it.isNotEmpty()) "$it%" else "89%" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenOwnerMenu) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = JarvisCyan)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "J.A.R.V.I.S.",
                    color = when (state.orbState) {
                        OrbState.THINKING -> JarvisAmberBright
                        OrbState.SPEAKING -> JarvisMagenta
                        else -> JarvisCyan
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = when (state.orbState) {
                        OrbState.LISTENING -> "LISTENING..."
                        OrbState.THINKING -> "THINKING..."
                        OrbState.SPEAKING -> "SPEAKING..."
                        else -> "LOCAL AI CORE v1.1"
                    },
                    color = when (state.orbState) {
                        OrbState.THINKING -> JarvisAmber
                        OrbState.SPEAKING -> JarvisPurple
                        else -> JarvisTextSecondary
                    },
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = JarvisCyan)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (state.orbState) {
            OrbState.THINKING -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JarvisCoreOrb(state = OrbState.THINKING, size = 200.dp)
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(JarvisCard)
                            .border(1.dp, JarvisAmber.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Text("> Processing your request...", color = JarvisAmberBright, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("> Accessing local modules...", color = JarvisTextSecondary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("> Running command...", color = JarvisTextSecondary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("> Preparing response...", color = JarvisTextSecondary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(14.dp))
                            LinearProgressIndicator(
                                progress = { 0.76f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = JarvisAmberBright,
                                trackColor = JarvisCardBorder
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Shield", tint = JarvisAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Give me a moment...", color = JarvisTextSecondary, fontSize = 13.sp)
                    }
                }
            }

            OrbState.SPEAKING -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JarvisCoreOrb(state = OrbState.SPEAKING, audioLevel = state.audioLevel, size = 200.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    WaveformVisualizer(isActive = true, audioLevel = state.audioLevel)
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(JarvisCard)
                            .border(1.dp, JarvisMagenta.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (state.lastResponse.isNotEmpty()) state.lastResponse else "Here's what I found for you...",
                            color = JarvisTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(JarvisMagenta.copy(alpha = 0.2f))
                                .border(2.dp, JarvisMagenta, CircleShape)
                                .clickable { viewModel.stopSpeaking() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = JarvisMagenta, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Tap to stop", color = JarvisTextSecondary, fontSize = 11.sp)
                    }
                }
            }

            OrbState.LISTENING -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JarvisCoreOrb(state = OrbState.LISTENING, audioLevel = state.audioLevel, size = 200.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    WaveformVisualizer(isActive = true, audioLevel = state.audioLevel)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Listening... Speak now", color = JarvisCyan, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.onMicrophoneClicked() },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = JarvisTextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel", color = JarvisTextPrimary, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap to stop", color = JarvisTextSecondary, fontSize = 11.sp)
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(JarvisCard)
                            .border(1.dp, JarvisCardBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenOwnerMenu)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = "Owner", tint = JarvisCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("OWNER", color = JarvisTextSecondary, fontSize = 9.sp)
                                Text("RTGYASH", color = JarvisTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SystemStatusBadge("SYSTEM", "ONLINE", isOnline = true)
                            SystemStatusBadge("BATTERY", batteryText)
                            SystemStatusBadge("TIME", timeFormat)
                            SystemStatusBadge("DATE", dateFormat)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickSideBadge("Apps", Icons.Default.Apps) { onNavigateToTools() }
                            QuickSideBadge("Web", Icons.Default.Language) { viewModel.processQuery("Search Google") }
                            QuickSideBadge("Tools", Icons.Default.Widgets) { onNavigateToTools() }
                            QuickSideBadge("Files", Icons.Default.Folder) { viewModel.processQuery("Open files") }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        JarvisCoreOrb(state = OrbState.IDLE, size = 190.dp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("JARVIS", color = JarvisCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("\"How can I help you today?\"", color = JarvisTextPrimary, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(JarvisBlue)
                                .border(3.dp, JarvisCyan, CircleShape)
                                .clickable { viewModel.onMicrophoneClicked() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Speak", tint = JarvisTextPrimary, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActionPillButton("Voice", "Tap to speak", Icons.Default.Mic) { viewModel.onMicrophoneClicked() }
                        ActionPillButton("Chat", "Type command", Icons.Default.Chat, onNavigateToChat)
                        ActionPillButton("Apps", "Open apps", Icons.Default.Apps, onNavigateToTools)
                        ActionPillButton("More", "Quick tools", Icons.Default.MoreHoriz, onOpenOwnerMenu)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Ask J.A.R.V.I.S. or control screen...", color = JarvisTextSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
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
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedContainerColor = JarvisCard,
                            unfocusedContainerColor = JarvisCard
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(JarvisSuccess)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("STANDBY", color = JarvisSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Text(
                text = "INTELLIGENCE • ASSISTANCE • ALWAYS",
                color = JarvisTextSecondary,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SystemStatusBadge(label: String, value: String, isOnline: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisCard)
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Column {
            Text(label, color = JarvisTextSecondary, fontSize = 9.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isOnline) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(JarvisSuccess))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(value, color = if (isOnline) JarvisSuccess else JarvisCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickSideBadge(label: String, icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisCard)
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = JarvisCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = JarvisTextPrimary, fontSize = 11.sp)
        }
    }
}

@Composable
fun ActionPillButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisCard)
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = title, tint = JarvisCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, color = JarvisTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = JarvisTextSecondary, fontSize = 8.sp)
        }
    }
}
