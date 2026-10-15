package com.example.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PermissionMode
import com.example.ui.components.CodeBadge
import com.example.ui.theme.BackgroundDark
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
fun SettingsScreen(
    currentPermissionMode: String,
    onPermissionModeChange: (String) -> Unit,
    onResetSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var maxSteps by remember { mutableFloatStateOf(25f) }
    var autoSnapshot by remember { mutableStateOf(true) }

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
            // Header Info
            item {
                Text(
                    text = "AGENT PREFERENCES",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            // Default Permission Mode
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security permissions",
                                tint = NavioCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Default Session Permission",
                                color = TextPrimaryDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Controls whether the model can execute file write/edit tools without manual confirmation.",
                            color = TextTertiaryDark,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            PermissionMode.values().forEach { mode ->
                                val isSelected = mode.label == currentPermissionMode
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SurfaceContainerHighDark else BackgroundDark)
                                        .border(
                                            1.dp,
                                            if (isSelected) NavioCyan else SurfaceBorderDark,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onPermissionModeChange(mode.label) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = mode.label,
                                            color = if (isSelected) TextPrimaryDark else TextSecondaryDark,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        Text(
                                            text = mode.description,
                                            color = TextTertiaryDark,
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = NavioCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Agent Loop Step Limit (Default 25)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Loop,
                                    contentDescription = "Step limit",
                                    tint = NavioViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Agent Step Limit",
                                    color = TextPrimaryDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            CodeBadge(
                                text = "${maxSteps.toInt()} steps",
                                color = NavioViolet,
                                backgroundColor = NavioViolet.copy(alpha = 0.15f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Maximum agent loop iterations before stopping (default 25 per agent run).",
                            color = TextTertiaryDark,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = maxSteps,
                            onValueChange = { maxSteps = it },
                            valueRange = 5f..50f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = NavioViolet,
                                activeTrackColor = NavioViolet,
                                inactiveTrackColor = SurfaceBorderDark
                            ),
                            modifier = Modifier.testTag("step_limit_slider")
                        )
                    }
                }
            }

            // Storage & Security Section Header
            item {
                Text(
                    text = "DEVICE STORAGE & SECURITY",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Device Workspace Card
            item {
                SettingsInfoRow(
                    icon = Icons.Default.Folder,
                    iconTint = NavioCyan,
                    title = "Workspace Document Sandbox",
                    subtitle = "Internal app storage",
                    badge = "~/data/workspaces/"
                )
            }

            // Keystore Status Card
            item {
                SettingsInfoRow(
                    icon = Icons.Default.Security,
                    iconTint = NavioEmerald,
                    title = "Hardware Keystore Encryption",
                    subtitle = "API credentials secured on-chip",
                    badge = "ACTIVE"
                )
            }

            // SQLite Database Status
            item {
                SettingsInfoRow(
                    icon = Icons.Default.Storage,
                    iconTint = NavioCyan,
                    title = "On-Device SQLite Database",
                    subtitle = "Local sessions and chat messages",
                    badge = "READY"
                )
            }

            // Dark Theme Status
            item {
                SettingsInfoRow(
                    icon = Icons.Default.DarkMode,
                    iconTint = NavioViolet,
                    title = "Theme Direction",
                    subtitle = "Dark developer studio theme with Monospace accents",
                    badge = "DARK THEME"
                )
            }

            // Reset Sample Data CTA
            item {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sample Data & Mock State",
                                color = TextPrimaryDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Reset placeholder sessions and models",
                                color = TextTertiaryDark,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onResetSampleData,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceContainerHighDark,
                                contentColor = NavioCyan
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("reset_sample_data_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cached,
                                contentDescription = "Reset data",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String
) {
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
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerHighDark)
                    .border(1.dp, SurfaceBorderDark, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimaryDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = TextTertiaryDark,
                    fontSize = 12.sp
                )
            }

            CodeBadge(
                text = badge,
                color = iconTint,
                backgroundColor = iconTint.copy(alpha = 0.12f)
            )
        }
    }
}
