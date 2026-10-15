package com.example.ui.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.NavioSession
import com.example.ui.components.SearchInputBar
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NavioCyan
import com.example.ui.theme.NavioCyanContainer
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TextTertiaryDark

@Composable
fun SessionsScreen(
    sessions: List<NavioSession>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSessionClick: (NavioSession) -> Unit,
    onNewSessionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Claude", "OpenAI", "DeepSeek")

    // Filter sessions by query and model tag
    val filteredSessions = remember(sessions, searchQuery, selectedFilter) {
        sessions.filter { session ->
            val matchesQuery = searchQuery.isBlank() ||
                session.title.contains(searchQuery, ignoreCase = true) ||
                session.modelName.contains(searchQuery, ignoreCase = true) ||
                session.workspacePath.contains(searchQuery, ignoreCase = true) ||
                session.previewText.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "Claude" -> session.modelName.contains("claude", ignoreCase = true)
                "OpenAI" -> session.modelName.contains("gpt", ignoreCase = true)
                "DeepSeek" -> session.modelName.contains("deepseek", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Box
            item {
                SearchInputBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClear = { onSearchQueryChange("") }
                )
            }

            // Quick Filter Chips + Session Count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) NavioCyanContainer else SurfaceContainerDark
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NavioCyan else SurfaceBorderDark,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) NavioCyan else TextSecondaryDark,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "${filteredSessions.size} of ${sessions.size} sessions",
                        color = TextTertiaryDark,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Quick New Session Banner CTA
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHighDark)
                        .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start a coding task",
                            color = TextPrimaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Direct model streaming & local workspace file edits",
                            color = TextTertiaryDark,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = onNewSessionClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavioCyan,
                            contentColor = Color(0xFF090D16)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("new_session_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Session",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.new_session),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "ACTIVE SESSIONS",
                    color = TextSecondaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            // Empty state if no sessions match
            if (filteredSessions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceContainerDark)
                                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Empty state icon",
                                tint = TextTertiaryDark,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(id = R.string.empty_sessions_title),
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "No sessions matched \"$searchQuery\". Try checking the spelling or resetting the search query."
                            } else {
                                stringResource(id = R.string.empty_sessions_desc)
                            },
                            color = TextTertiaryDark,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        if (searchQuery.isNotEmpty() || selectedFilter != "All") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    onSearchQueryChange("")
                                    selectedFilter = "All"
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceContainerHighDark,
                                    contentColor = NavioCyan
                                )
                            ) {
                                Text("Reset Filters")
                            }
                        }
                    }
                }
            } else {
                items(filteredSessions, key = { it.id }) { session ->
                    SessionCard(
                        session = session,
                        onClick = { onSessionClick(session) }
                    )
                }
            }
        }

        // Floating Action Button for quick creation
        FloatingActionButton(
            onClick = onNewSessionClick,
            containerColor = NavioCyan,
            contentColor = Color(0xFF090D16),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("new_session_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create New Session",
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
