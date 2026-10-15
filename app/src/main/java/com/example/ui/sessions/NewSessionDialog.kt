package com.example.ui.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.PermissionMode
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NavioCyan
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@Composable
fun NewSessionDialog(
    onDismiss: () -> Unit,
    onCreateSession: (title: String, model: String, workspace: String, permissionMode: String) -> Unit,
    availableModels: List<String> = listOf(
        "claude-3-7-sonnet",
        "gpt-4o",
        "deepseek-chat",
        "claude-3-5-haiku"
    )
) {
    var title by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf(availableModels.firstOrNull() ?: "claude-3-7-sonnet") }
    var workspaceFolder by remember { mutableStateOf("navio-workspace-1") }
    var selectedPermission by remember { mutableStateOf(PermissionMode.ASK_BEFORE_WRITES) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceContainerDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "New Coding Session",
                            color = TextPrimaryDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Runs on-device with direct model APIs",
                            color = TextTertiaryDark,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog",
                            tint = TextTertiaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Session Goal / Title
                Text(
                    text = "SESSION TITLE / TASK",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text(
                            text = "e.g., Refactor SQLite migrations to v2",
                            color = TextTertiaryDark,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavioCyan,
                        unfocusedBorderColor = SurfaceBorderDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Select AI Model
                Text(
                    text = "TARGET MODEL",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    availableModels.forEach { model ->
                        val isSelected = model == selectedModel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SurfaceContainerHighDark else BackgroundDark)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NavioCyan else SurfaceBorderDark,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedModel = model }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedModel = model },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NavioCyan,
                                    unselectedColor = TextTertiaryDark
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = model,
                                color = if (isSelected) TextPrimaryDark else TextSecondaryDark,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Workspace directory
                Text(
                    text = "LOCAL WORKSPACE FOLDER",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = workspaceFolder,
                    onValueChange = { workspaceFolder = it },
                    prefix = {
                        Text(
                            text = "~/workspaces/",
                            color = NavioCyan,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workspace_folder_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavioCyan,
                        unfocusedBorderColor = SurfaceBorderDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Permission Mode
                Text(
                    text = "TOOL PERMISSION MODE",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PermissionMode.values().forEach { mode ->
                        val isSelected = mode == selectedPermission
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SurfaceContainerHighDark else BackgroundDark)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NavioCyan else SurfaceBorderDark,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPermission = mode }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPermission = mode },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NavioCyan,
                                    unselectedColor = TextTertiaryDark
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = mode.label,
                                    color = if (isSelected) TextPrimaryDark else TextSecondaryDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = mode.description,
                                    color = TextTertiaryDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryDark)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val finalTitle = title.trim().ifEmpty { "New Coding Session" }
                            onCreateSession(
                                finalTitle,
                                selectedModel,
                                "~/workspaces/${workspaceFolder.trim().ifEmpty { "project" }}",
                                selectedPermission.label
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavioCyan,
                            contentColor = Color(0xFF090D16)
                        ),
                        modifier = Modifier.testTag("create_session_button")
                    ) {
                        Text("Create Session", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
