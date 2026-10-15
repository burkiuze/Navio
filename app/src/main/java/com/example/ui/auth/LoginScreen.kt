package com.example.ui.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthProvider
import com.example.ui.components.NavioLogo
import com.example.ui.theme.Background
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LoginScreen(busy: Boolean, error: String?, onSignIn: (AuthProvider) -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NavioLogo(48.dp)
            Spacer(Modifier.height(20.dp))
            Text("Navio'ya hoş geldin", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Sor, araştır ve işlerini otomasyonlara devret.",
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(36.dp))
            if (busy) {
                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
            }
            AuthProvider.entries.forEach { provider ->
                ProviderButton(provider, enabled = !busy) { onSignIn(provider) }
                Spacer(Modifier.height(12.dp))
            }
            if (error != null) {
                Spacer(Modifier.height(16.dp))
                Text(error, color = ErrorRed, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ProviderButton(provider: AuthProvider, enabled: Boolean, onClick: () -> Unit) {
    val (mark, markColor) = when (provider) {
        AuthProvider.GOOGLE -> "G" to Color(0xFF4285F4)
        AuthProvider.GITHUB -> "\u25CF" to Color(0xFF1F1F1F)
        AuthProvider.GITLAB -> "\u25B2" to Color(0xFFFC6D26)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("sign_in_${provider.name.lowercase()}"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(mark, color = markColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Text("${provider.label} ile devam et", color = Color(0xFF1F1F1F), fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
