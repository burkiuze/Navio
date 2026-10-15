package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.chat.ChatScreen
import com.example.ui.components.ModeSwitcher
import com.example.ui.components.PromptBar
import com.example.ui.computer.ComputerScreen
import com.example.ui.sheets.HistorySheet
import com.example.ui.sheets.NewAutomationSheet
import com.example.ui.sheets.SettingsSheet
import com.example.ui.theme.Background
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppMode
import com.example.viewmodel.NavioViewModel

private enum class Sheet { HISTORY, SETTINGS, NEW_AUTOMATION }

@Composable
fun NavioApp(viewModel: NavioViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbar.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    var sheet by remember { mutableStateOf<Sheet?>(null) }
    val settings = state.settings ?: return

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    Scaffold(
        containerColor = Background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            // No top-left control: the mode switch sits centered, history/settings on the right.
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(WindowInsetsTop)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                ModeSwitcher(state.mode, viewModel::setMode, Modifier.align(Alignment.Center))
                Row(Modifier.align(Alignment.CenterEnd)) {
                    if (state.mode == AppMode.CHAT) {
                        IconButton(onClick = { sheet = Sheet.HISTORY }) {
                            Icon(Icons.Outlined.History, "Sohbetler", tint = TextSecondary)
                        }
                    }
                    IconButton(onClick = { sheet = Sheet.SETTINGS }) {
                        Icon(Icons.Outlined.Settings, "Ayarlar", tint = TextSecondary)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ) {
                when (state.mode) {
                    AppMode.CHAT -> {
                        ChatScreen(
                            conversation = state.conversation,
                            automations = state.automations,
                            busy = state.chatBusy,
                            webSearch = settings.webSearch,
                            onSuggestion = viewModel::sendChat,
                            onOpenAutomation = { viewModel.setMode(AppMode.COMPUTER) },
                            modifier = Modifier.weight(1f)
                        )
                        PromptBar(
                            placeholder = if (state.conversation == null) "Her şeyi sorun…" else "Devam sorusu sorun…",
                            busy = state.chatBusy,
                            onSend = viewModel::sendChat,
                            onLeading = viewModel::newChat,
                            model = settings.model,
                            onModelChange = viewModel::setModel,
                            webSearch = settings.webSearch,
                            onWebSearchChange = viewModel::setWebSearch
                        )
                    }
                    AppMode.COMPUTER -> {
                        ComputerScreen(
                            automations = state.automations,
                            reply = state.computerReply,
                            onDismissReply = viewModel::dismissComputerReply,
                            onNewAutomation = { sheet = Sheet.NEW_AUTOMATION },
                            onToggle = viewModel::setAutomationEnabled,
                            onRunNow = viewModel::runAutomationNow,
                            onDelete = viewModel::deleteAutomation,
                            modifier = Modifier.weight(1f)
                        )
                        PromptBar(
                            placeholder = "Her şeyi yapın… (ör. her sabah 9'da haberleri özetle)",
                            busy = state.computerBusy,
                            onSend = viewModel::sendComputer,
                            onLeading = { sheet = Sheet.NEW_AUTOMATION },
                            accent = true
                        )
                    }
                }
            }
        }
    }

    when (sheet) {
        Sheet.HISTORY -> HistorySheet(
            conversations = state.conversations,
            onOpen = {
                viewModel.openConversation(it)
                sheet = null
            },
            onDelete = viewModel::deleteConversation,
            onDismiss = { sheet = null }
        )
        Sheet.SETTINGS -> SettingsSheet(settings, viewModel::saveSettings) { sheet = null }
        Sheet.NEW_AUTOMATION -> NewAutomationSheet(viewModel::createAutomation) { sheet = null }
        null -> Unit
    }
}

// Scaffold doesn't inset the topBar slot; the custom bar applies the status-bar inset itself.
private val WindowInsetsTop: PaddingValues
    @Composable get() = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        .asPaddingValues()
