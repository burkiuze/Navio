package com.example.ui.chat

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavioSession
import com.example.ui.components.CodeBadge
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NavioCyan
import com.example.ui.theme.NavioEmerald
import com.example.ui.theme.NavioViolet
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@Composable
fun SessionDetailPreview(
    session: NavioSession,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Session Header
        Surface(
            color = SurfaceContainerDark,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("session_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to sessions",
                        tint = TextPrimaryDark
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Workspace",
                            tint = NavioCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = session.workspacePath,
                            color = TextTertiaryDark,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                CodeBadge(
                    text = session.modelName,
                    color = NavioCyan,
                    backgroundColor = NavioCyan.copy(alpha = 0.15f)
                )
            }
        }

        // Chat Message Stream Preview
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Phase status note banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerHighDark)
                        .border(1.dp, SurfaceBorderDark, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "⚡ Phase 1 Shell Active • Direct streaming and tool execution will connect in Phase 2 & 3.",
                        color = NavioCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            // User Query Bubble
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                            .background(SurfaceContainerHighDark)
                            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Please examine the project files and implement the requested algorithm with clean test coverage.",
                            color = TextPrimaryDark,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Assistant Tool Call Card Preview (Phase 4 preview)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = "Tool call",
                                    tint = NavioEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "tool_call: write_file",
                                    color = NavioEmerald,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            CodeBadge(
                                text = "APPROVED",
                                color = NavioEmerald,
                                backgroundColor = NavioEmerald.copy(alpha = 0.15f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "path: \"${session.workspacePath}/src/Algorithm.kt\"",
                            color = TextSecondaryDark,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Assistant Response with Monospace Code Block
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(0.95f),
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NavioCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = session.modelName,
                                color = NavioCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "I've written the initial implementation to your local workspace directory. Here is the core logic:",
                            color = TextPrimaryDark,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Monospace Code Block
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BackgroundDark)
                                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = """// On-device local file
fun <T> Flow<T>.throttleLatest(periodMillis: Long): Flow<T> = channelFlow {
    var lastValue: T? = null
    collectLatest { value ->
        lastValue = value
        delay(periodMillis)
        send(value)
    }
}""",
                                color = Color(0xFFE2E8F0),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Composer Bar at the bottom
        Surface(
            color = SurfaceContainerDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CodeBadge(
                    text = session.modelName.substringBefore("-").uppercase(),
                    color = NavioViolet,
                    backgroundColor = NavioViolet.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainerHighDark)
                        .border(1.dp, SurfaceBorderDark, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Message ${session.modelName}…",
                        color = TextTertiaryDark,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { /* Enabled in Phase 2 & 3 */ },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NavioCyan)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF090D16),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
