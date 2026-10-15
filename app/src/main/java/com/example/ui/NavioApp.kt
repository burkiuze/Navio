package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.chat.SessionDetailPreview
import com.example.ui.components.NavioBottomBar
import com.example.ui.components.NavioTopBar
import com.example.ui.models.ModelsScreen
import com.example.ui.navigation.NavTab
import com.example.ui.sessions.NewSessionDialog
import com.example.ui.sessions.SessionsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.BackgroundDark
import com.example.viewmodel.NavioViewModel

@Composable
fun NavioApp(
    viewModel: NavioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (uiState.selectedSession == null) {
                NavioTopBar(
                    onNewSessionClick = if (uiState.currentTab == NavTab.SESSIONS) {
                        { viewModel.onSetShowNewSessionDialog(true) }
                    } else null
                )
            }
        },
        bottomBar = {
            if (uiState.selectedSession == null) {
                NavioBottomBar(
                    currentTab = uiState.currentTab,
                    onTabSelected = { viewModel.onTabSelected(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.selectedSession != null) {
                SessionDetailPreview(
                    session = uiState.selectedSession!!,
                    onBack = { viewModel.onSelectSession(null) }
                )
            } else {
                when (uiState.currentTab) {
                    NavTab.SESSIONS -> {
                        SessionsScreen(
                            sessions = uiState.sessions,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                            onSessionClick = { viewModel.onSelectSession(it) },
                            onNewSessionClick = { viewModel.onSetShowNewSessionDialog(true) }
                        )
                    }

                    NavTab.MODELS -> {
                        ModelsScreen(
                            models = uiState.models,
                            providers = uiState.providers,
                            defaultModelId = uiState.defaultModelId,
                            onSetDefaultModel = { viewModel.setDefaultModel(it) },
                            onAddProvider = { name, protocol, url, key ->
                                viewModel.addProvider(name, protocol, url, key)
                            }
                        )
                    }

                    NavTab.SETTINGS -> {
                        SettingsScreen(
                            currentPermissionMode = uiState.defaultPermissionMode,
                            onPermissionModeChange = { viewModel.updatePermissionMode(it) },
                            onResetSampleData = { viewModel.resetSampleData() }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showNewSessionDialog) {
        NewSessionDialog(
            onDismiss = { viewModel.onSetShowNewSessionDialog(false) },
            onCreateSession = { title, model, workspace, mode ->
                viewModel.createSession(title, model, workspace, mode)
            },
            availableModels = uiState.models.map { it.id }
        )
    }
}
