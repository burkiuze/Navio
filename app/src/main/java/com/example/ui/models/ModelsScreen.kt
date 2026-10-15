package com.example.ui.models

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.NavioModel
import com.example.model.NavioProvider
import com.example.ui.components.CodeBadge
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NavioAmber
import com.example.ui.theme.NavioCyan
import com.example.ui.theme.NavioCyanContainer
import com.example.ui.theme.NavioEmerald
import com.example.ui.theme.NavioViolet
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@Composable
fun ModelsScreen(
    models: List<NavioModel>,
    providers: List<NavioProvider>,
    defaultModelId: String,
    onSetDefaultModel: (String) -> Unit,
    onAddProvider: (name: String, protocol: String, baseUrl: String, apiKey: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddProviderDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Security & On-Device Info Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Secure storage",
                                tint = NavioEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ZERO-BACKEND ARCHITECTURE",
                                color = NavioEmerald,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Navio calls AI providers directly from your device. API keys are encrypted in hardware-backed Android Keystore and never logged or proxied.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Providers Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONFIGURED PROVIDERS",
                        color = TextSecondaryDark,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )

                    Button(
                        onClick = { showAddProviderDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceContainerHighDark,
                            contentColor = NavioCyan
                        ),
                        modifier = Modifier.testTag("add_provider_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Provider",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Provider", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Provider Cards
            items(providers, key = { it.id }) { provider ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainerHighDark)
                                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsInputAntenna,
                                contentDescription = provider.name,
                                tint = if (provider.isConfigured) NavioCyan else TextTertiaryDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = provider.name,
                                    color = TextPrimaryDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (provider.isConfigured) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NavioEmerald.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "READY",
                                            color = NavioEmerald,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${provider.protocol} • ${provider.baseUrl}",
                                color = TextTertiaryDark,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        // Key Status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Key status",
                                tint = if (provider.isConfigured) NavioCyan else TextTertiaryDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = provider.maskedKey,
                                color = TextSecondaryDark,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Models Section Header
            item {
                Text(
                    text = "AVAILABLE MODELS & DEFAULT CHOICE",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // List of Models
            items(models, key = { it.id }) { model ->
                val isDefault = model.id == defaultModelId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = if (isDefault) NavioCyan else SurfaceBorderDark,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSetDefaultModel(model.id) }
                        .testTag("model_item_${model.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDefault) SurfaceContainerHighDark else SurfaceContainerDark
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isDefault,
                                    onClick = { onSetDefaultModel(model.id) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = NavioCyan,
                                        unselectedColor = TextTertiaryDark
                                    ),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = model.name,
                                        color = TextPrimaryDark,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = model.id,
                                        color = NavioCyan,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (isDefault) {
                                CodeBadge(
                                    text = "DEFAULT",
                                    color = NavioCyan,
                                    backgroundColor = NavioCyanContainer.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Features & specs row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CodeBadge(
                                text = model.contextWindow,
                                color = TextSecondaryDark,
                                backgroundColor = BackgroundDark,
                                borderColor = SurfaceBorderDark
                            )

                            if (model.supportsTools) {
                                CodeBadge(
                                    text = "tool_calls",
                                    color = NavioEmerald,
                                    backgroundColor = NavioEmerald.copy(alpha = 0.15f),
                                    borderColor = NavioEmerald.copy(alpha = 0.3f)
                                )
                            }

                            if (model.supportsStreaming) {
                                CodeBadge(
                                    text = "streaming",
                                    color = NavioViolet,
                                    backgroundColor = NavioViolet.copy(alpha = 0.15f),
                                    borderColor = NavioViolet.copy(alpha = 0.3f)
                                )
                            }
                        }

                        if (model.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = model.description,
                                color = TextTertiaryDark,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddProviderDialog) {
        AddProviderDialog(
            onDismiss = { showAddProviderDialog = false },
            onConfirm = { name, protocol, baseUrl, key ->
                onAddProvider(name, protocol, baseUrl, key)
                showAddProviderDialog = false
            }
        )
    }
}

@Composable
private fun AddProviderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, protocol: String, baseUrl: String, key: String) -> Unit
) {
    var name by remember { mutableStateOf("OpenRouter") }
    var protocol by remember { mutableStateOf("OpenAI-compatible") }
    var baseUrl by remember { mutableStateOf("https://openrouter.ai/api/v1") }
    var apiKey by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceContainerDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add Model Provider",
                    color = TextPrimaryDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connect custom OpenAI-compatible or Anthropic endpoints",
                    color = TextTertiaryDark,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PROVIDER NAME",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavioCyan,
                        unfocusedBorderColor = SurfaceBorderDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "BASE URL",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavioCyan,
                        unfocusedBorderColor = SurfaceBorderDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "API KEY (SECURE STORAGE)",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    placeholder = { Text("sk-...", color = TextTertiaryDark) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavioCyan,
                        unfocusedBorderColor = SurfaceBorderDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(name, protocol, baseUrl, apiKey) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavioCyan,
                            contentColor = Color(0xFF090D16)
                        )
                    ) {
                        Text("Save Provider", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
