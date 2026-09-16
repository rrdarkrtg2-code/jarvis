package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ui.components.JarvisCoreOrb
import com.jarvis.assistant.ui.components.OrbState
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary

@Composable
fun OnboardingScreen(onFinishOnboarding: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(JarvisCard)
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            JarvisCoreOrb(state = OrbState.IDLE, size = 160.dp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WELCOME TO J.A.R.V.I.S.",
                color = JarvisCyan,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your personal AI Operating Assistant. Ready to control hardware, launch applications, schedule reminders, remember preferences, and answer complex queries.",
                color = JarvisTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onFinishOnboarding,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("INITIALIZE SYSTEM", color = JarvisBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}
