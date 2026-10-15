package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.automation.AutomationScheduler
import com.example.data.AgentMode
import com.example.data.AuthManager
import com.example.data.AuthProvider
import com.example.data.Automation
import com.example.data.ChatMessage
import com.example.data.Conversation
import com.example.data.NavioAgent
import com.example.data.NavioStore
import com.example.data.NavioUser
import com.example.data.OpenRouterClient
import com.example.data.Role
import com.example.data.Schedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppMode { CHAT, COMPUTER }

data class ComputerReply(val text: String, val isError: Boolean = false)

data class NavioUiState(
    val mode: AppMode = AppMode.CHAT,
    val conversation: Conversation? = null,
    val conversations: List<Conversation> = emptyList(),
    val automations: List<Automation> = emptyList(),
    val settings: NavioStore.Settings? = null,
    val chatBusy: Boolean = false,
    val computerBusy: Boolean = false,
    val computerReply: ComputerReply? = null,
    val user: NavioUser? = null,
    val authBusy: Boolean = false,
    val authError: String? = null,
    val models: List<Pair<String, String>> = emptyList()
)

private data class LocalState(
    val mode: AppMode = AppMode.CHAT,
    val conversationId: String? = null,
    val chatBusy: Boolean = false,
    val computerBusy: Boolean = false,
    val computerReply: ComputerReply? = null,
    val authBusy: Boolean = false,
    val authError: String? = null,
    val models: List<Pair<String, String>> = emptyList()
)

class NavioViewModel(application: Application) : AndroidViewModel(application) {

    private val store = NavioStore.get(application)
    private val agent = NavioAgent(application)
    private val auth = AuthManager.get(application)
    private val local = MutableStateFlow(LocalState())

    val uiState: StateFlow<NavioUiState> =
        combine(local, store.conversations, store.automations, store.settings, auth.user) { l, conversations, automations, settings, user ->
            NavioUiState(
                mode = l.mode,
                conversation = conversations.firstOrNull { it.id == l.conversationId },
                conversations = conversations,
                automations = automations,
                settings = settings,
                chatBusy = l.chatBusy,
                computerBusy = l.computerBusy,
                computerReply = l.computerReply,
                user = user,
                authBusy = l.authBusy,
                authError = l.authError,
                models = l.models
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, NavioUiState(settings = store.settings.value, user = auth.user.value))

    init {
        viewModelScope.launch {
            val models = OpenRouterClient.listModels()
            if (models.isNotEmpty()) local.update { it.copy(models = models) }
        }
    }

    // region auth

    /** Starts OAuth with [provider]; [openBrowser] receives the provider URL. */
    fun signIn(provider: AuthProvider, openBrowser: (Uri) -> Unit) {
        local.update { it.copy(authBusy = true, authError = null) }
        viewModelScope.launch {
            try {
                openBrowser(auth.startSignIn(provider))
            } catch (e: Exception) {
                local.update { it.copy(authBusy = false, authError = e.message ?: "Giriş başlatılamadı.") }
            }
        }
    }

    /** Handles `navio://auth?code=…`; returns false if [uri] isn't an auth callback. */
    fun handleAuthCallback(uri: Uri?): Boolean {
        if (!auth.isAuthCallback(uri)) return false
        viewModelScope.launch {
            val error = auth.completeSignIn(uri!!)
            local.update { it.copy(authBusy = false, authError = error) }
        }
        return true
    }

    /** User came back from the browser without finishing sign-in. */
    fun onResumeWithoutCallback() {
        if (local.value.authBusy && auth.user.value == null) local.update { it.copy(authBusy = false) }
    }

    fun signOut() {
        viewModelScope.launch { auth.signOut() }
    }

    // endregion

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar.asStateFlow()

    fun consumeSnackbar() {
        _snackbar.value = null
    }

    fun setMode(mode: AppMode) = local.update { it.copy(mode = mode) }

    // region chat

    fun newChat() = local.update { it.copy(conversationId = null, mode = AppMode.CHAT) }

    fun openConversation(id: String) = local.update { it.copy(conversationId = id, mode = AppMode.CHAT) }

    fun deleteConversation(id: String) {
        store.deleteConversation(id)
        if (local.value.conversationId == id) local.update { it.copy(conversationId = null) }
    }

    fun setWebSearch(enabled: Boolean) = store.updateSettings { it.copy(webSearch = enabled) }

    fun setModel(model: String) = store.updateSettings { it.copy(model = model) }

    fun sendChat(text: String) {
        val prompt = text.trim()
        if (prompt.isEmpty() || local.value.chatBusy) return
        val existing = local.value.conversationId?.let(store::conversation)
        val userMessage = ChatMessage(UUID.randomUUID().toString(), Role.USER, prompt)
        val conversation = (existing ?: Conversation(
            id = UUID.randomUUID().toString(),
            title = prompt.lineSequence().first().take(60),
            messages = emptyList()
        )).let { it.copy(messages = it.messages + userMessage, updatedAt = System.currentTimeMillis()) }
        store.upsertConversation(conversation)
        local.update { it.copy(conversationId = conversation.id, chatBusy = true) }

        viewModelScope.launch {
            val reply = try {
                val result = agent.reply(conversation.messages, store.settings.value.webSearch, AgentMode.CHAT)
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = Role.ASSISTANT,
                    content = result.content,
                    sources = result.sources,
                    createdAutomationIds = result.createdAutomations.map { it.id }
                )
            } catch (e: Exception) {
                ChatMessage(UUID.randomUUID().toString(), Role.ASSISTANT, e.message ?: e.toString(), isError = true)
            }
            val latest = store.conversation(conversation.id) ?: conversation
            store.upsertConversation(latest.copy(messages = latest.messages + reply, updatedAt = System.currentTimeMillis()))
            local.update { it.copy(chatBusy = false) }
        }
    }

    // endregion

    // region computer

    /** Natural-language automation setup from the Computer tab. */
    fun sendComputer(text: String) {
        val prompt = text.trim()
        if (prompt.isEmpty() || local.value.computerBusy) return
        local.update { it.copy(computerBusy = true, computerReply = null) }
        viewModelScope.launch {
            val reply = try {
                val result = agent.reply(
                    listOf(ChatMessage(UUID.randomUUID().toString(), Role.USER, prompt)),
                    webSearch = false,
                    mode = AgentMode.COMPUTER
                )
                ComputerReply(result.content)
            } catch (e: Exception) {
                ComputerReply(e.message ?: e.toString(), isError = true)
            }
            local.update { it.copy(computerBusy = false, computerReply = reply) }
        }
    }

    fun dismissComputerReply() = local.update { it.copy(computerReply = null) }

    fun createAutomation(name: String, prompt: String, schedule: Schedule, useWebSearch: Boolean) {
        val automation = Automation(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifBlank { "Otomasyon" },
            prompt = prompt.trim(),
            schedule = schedule,
            useWebSearch = useWebSearch
        )
        store.upsertAutomation(automation)
        AutomationScheduler.schedule(getApplication(), automation)
        _snackbar.value = "\"${automation.name}\" kuruldu · ${schedule.describe()}"
    }

    fun setAutomationEnabled(id: String, enabled: Boolean) {
        val automation = store.automation(id)?.copy(enabled = enabled) ?: return
        store.upsertAutomation(automation)
        AutomationScheduler.schedule(getApplication(), automation)
    }

    fun runAutomationNow(id: String) {
        AutomationScheduler.runNow(getApplication(), id)
        _snackbar.value = "Otomasyon çalıştırılıyor…"
    }

    fun deleteAutomation(id: String) {
        AutomationScheduler.cancel(getApplication(), id)
        store.deleteAutomation(id)
    }

    // endregion

}
