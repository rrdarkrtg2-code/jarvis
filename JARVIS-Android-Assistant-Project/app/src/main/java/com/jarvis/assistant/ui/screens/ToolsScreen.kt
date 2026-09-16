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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import com.jarvis.assistant.ui.viewmodel.MainViewModel

@Composable
fun ToolsScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "SYSTEM CONTROLS & TOOLS",
            color = JarvisCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Direct hardware and Android automation triggers",
            color = JarvisTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Controls
        Text(text = "HARDWARE", color = JarvisCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolActionCard("Toggle Flashlight", "Torch On/Off", Modifier.weight(1f)) {
                viewModel.processQuery("Turn on flashlight")
            }
            ToolActionCard("Battery Status", "Check Power & Charge", Modifier.weight(1f)) {
                viewModel.processQuery("What is my battery")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolActionCard("Volume Up", "Raise Media Volume", Modifier.weight(1f)) {
                viewModel.processQuery("Volume up")
            }
            ToolActionCard("Volume Down", "Lower Media Volume", Modifier.weight(1f)) {
                viewModel.processQuery("Volume down")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation Automation
        Text(text = "NAVIGATION (ACCESSIBILITY)", color = JarvisCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolActionCard("Go Home", "Global Action Home", Modifier.weight(1f)) {
                viewModel.processQuery("Go home")
            }
            ToolActionCard("Go Back", "Global Action Back", Modifier.weight(1f)) {
                viewModel.processQuery("Go back")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolActionCard("Recent Apps", "Show Task Switcher", Modifier.weight(1f)) {
                viewModel.processQuery("Show recent apps")
            }
            ToolActionCard("Screenshot", "Capture Display", Modifier.weight(1f)) {
                viewModel.processQuery("Take a screenshot")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Online & Media Tools
        Text(text = "MEDIA & WEB", color = JarvisCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolActionCard("Open YouTube", "Launch Application", Modifier.weight(1f)) {
                viewModel.processQuery("Open YouTube")
            }
            ToolActionCard("Read Screen", "Accessibility OCR", Modifier.weight(1f)) {
                viewModel.processQuery("What does this screen say")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolActionCard("Read Notifications", "Active Alerts Summary", Modifier.weight(1f)) {
                viewModel.processQuery("Read my notifications")
            }
            ToolActionCard("Open Settings", "Android System Settings", Modifier.weight(1f)) {
                viewModel.processQuery("Open settings")
            }
        }
    }
}

@Composable
fun ToolActionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
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
            Text(text = title, color = JarvisTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = JarvisTextSecondary, fontSize = 11.sp)
        }
    }
}
