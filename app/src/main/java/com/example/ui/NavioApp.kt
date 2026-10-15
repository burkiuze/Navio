package com.example.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.components.ModeSwitcher
import com.example.ui.components.PromptBar
import com.example.ui.computer.ComputerScreen
import com.example.ui.sheets.NewAutomationSheet
import com.example.ui.sheets.SettingsSheet
import com.example.ui.sidebar.Sidebar
import com.example.ui.theme.Background
import com.example.ui.theme.Surface
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppMode
import com.example.viewmodel.NavioUiState
import com.example.viewmodel.NavioViewModel
import kotlinx.coroutines.launch

private enum class Sheet { SETTINGS, NEW_AUTOMATION }

@Composable
fun NavioApp(viewModel: NavioViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (state.user == null) {
        LoginScreen(busy = state.authBusy, error = state.authError) { provider ->
            viewModel.signIn(provider) { url -> context.startActivity(Intent(Intent.ACTION_VIEW, url)) }
        }
        return
    }

    var sheet by remember { mutableStateOf<Sheet?>(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BoxWithConstraints(Modifier.fillMaxSize().background(Background)) {
        val wide = maxWidth >= 840.dp
        val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

        val sidebar = @Composable { modifier: Modifier ->
            Sidebar(
                user = state.user,
                conversations = state.conversations,
                automationCount = state.automations.size,
                selectedConversationId = state.conversation?.id,
                mode = state.mode,
                onNewChat = { viewModel.newChat(); closeDrawer() },
                onOpenConversation = { viewModel.openConversation(it); closeDrawer() },
                onDeleteConversation = viewModel::deleteConversation,
                onOpenComputer = { viewModel.setMode(AppMode.COMPUTER); closeDrawer() },
                onOpenSettings = { sheet = Sheet.SETTINGS; closeDrawer() },
                modifier = modifier
            )
        }

        if (wide) {
            Row(Modifier.fillMaxSize()) {
                sidebar(
                    Modifier
                        .width(300.dp)
                        .padding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start).asPaddingValues())
                )
                MainPane(state, viewModel, onMenu = null, onSheet = { sheet = it })
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(drawerContainerColor = Surface, modifier = Modifier.width(300.dp)) {
                        sidebar(Modifier.fillMaxHeight())
                    }
                }
            ) {
                MainPane(state, viewModel, onMenu = { scope.launch { drawerState.open() } }, onSheet = { sheet = it })
            }
        }
    }

    when (sheet) {
        Sheet.SETTINGS -> state.settings?.let { settings ->
            SettingsSheet(
                settings = settings,
                user = state.user,
                models = state.models,
                onModelChange = viewModel::setModel,
                onSignOut = viewModel::signOut,
                onDismiss = { sheet = null }
            )
        }
        Sheet.NEW_AUTOMATION -> NewAutomationSheet(viewModel::createAutomation) { sheet = null }
        null -> Unit
    }
}

@Composable
private fun MainPane(
    state: NavioUiState,
    viewModel: NavioViewModel,
    onMenu: (() -> Unit)?,
    onSheet: (Sheet) -> Unit
) {
    val settings = state.settings ?: return
    val snackbarMessage by viewModel.snackbar.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

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
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(WindowInsetsTop)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (onMenu != null) {
                    IconButton(onClick = onMenu, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.Outlined.Menu, "Menü", tint = TextSecondary)
                    }
                }
                ModeSwitcher(state.mode, viewModel::setMode, Modifier.align(Alignment.Center))
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
                            models = state.models,
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
                            onNewAutomation = { onSheet(Sheet.NEW_AUTOMATION) },
                            onToggle = viewModel::setAutomationEnabled,
                            onRunNow = viewModel::runAutomationNow,
                            onDelete = viewModel::deleteAutomation,
                            modifier = Modifier.weight(1f)
                        )
                        PromptBar(
                            placeholder = "Her şeyi yapın… (ör. her sabah 9'da haberleri özetle)",
                            busy = state.computerBusy,
                            onSend = viewModel::sendComputer,
                            onLeading = { onSheet(Sheet.NEW_AUTOMATION) },
                            accent = true
                        )
                    }
                }
            }
        }
    }
}

// Scaffold doesn't inset the topBar slot; the custom bar applies the status-bar inset itself.
private val WindowInsetsTop: PaddingValues
    @Composable get() = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        .asPaddingValues()
