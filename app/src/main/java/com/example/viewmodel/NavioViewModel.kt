package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.NavioModel
import com.example.model.NavioProvider
import com.example.model.NavioSession
import com.example.ui.navigation.NavTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class NavioUiState(
    val sessions: List<NavioSession> = emptyList(),
    val models: List<NavioModel> = emptyList(),
    val providers: List<NavioProvider> = emptyList(),
    val searchQuery: String = "",
    val currentTab: NavTab = NavTab.SESSIONS,
    val defaultModelId: String = "claude-3-7-sonnet",
    val selectedSession: NavioSession? = null,
    val showNewSessionDialog: Boolean = false,
    val defaultPermissionMode: String = "Ask before writes"
)

class NavioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NavioUiState())
    val uiState: StateFlow<NavioUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val initialSessions = listOf(
            NavioSession(
                id = "sess-1",
                title = "Refactor SQLite schema to v2 migrations with type-safe queries",
                lastActivity = "2m ago",
                modelName = "claude-3-7-sonnet",
                workspacePath = "~/workspaces/navio-core",
                permissionMode = "Ask before writes",
                previewText = "db.execSQL(\"ALTER TABLE sessions ADD COLUMN model_config TEXT\")",
                messageCount = 8,
                status = "Idle"
            ),
            NavioSession(
                id = "sess-2",
                title = "Implement streaming diff parser & syntax highlighter",
                lastActivity = "18m ago",
                modelName = "gpt-4o",
                workspacePath = "~/workspaces/navio-core",
                permissionMode = "Auto-approve",
                previewText = "const lines = unifiedDiff.split('\\n'); return renderDiffTree(lines);",
                messageCount = 14,
                status = "Idle"
            ),
            NavioSession(
                id = "sess-3",
                title = "Fix JWT token refresh race condition in auth gateway",
                lastActivity = "1h ago",
                modelName = "claude-3-5-haiku",
                workspacePath = "~/projects/auth-gateway",
                permissionMode = "Ask before writes",
                previewText = "suspend fun refreshTokensMutex() = withLock { ... }",
                messageCount = 5,
                status = "Completed"
            ),
            NavioSession(
                id = "sess-4",
                title = "Benchmark agent loop tool call latencies vs OpenAI",
                lastActivity = "Yesterday",
                modelName = "deepseek-chat",
                workspacePath = "~/benchmarks/tool-latency",
                permissionMode = "Read-only",
                previewText = "Mean latency: 342ms with 99.8% schema adherence across 25 steps",
                messageCount = 22,
                status = "Completed"
            ),
            NavioSession(
                id = "sess-5",
                title = "Build interactive Canvas chart for token economics",
                lastActivity = "3 days ago",
                modelName = "gpt-4o",
                workspacePath = "~/visualizer/charts",
                permissionMode = "Ask before writes",
                previewText = "Canvas(modifier = Modifier.fillMaxSize()) { drawPath(...) }",
                messageCount = 11,
                status = "Idle"
            )
        )

        val initialModels = listOf(
            NavioModel(
                id = "claude-3-7-sonnet",
                name = "Claude 3.7 Sonnet",
                providerId = "anthropic",
                contextWindow = "200k tokens",
                isDefault = true,
                supportsTools = true,
                supportsStreaming = true,
                description = "Anthropic's flagship reasoning & coding model with extended thinking."
            ),
            NavioModel(
                id = "gpt-4o",
                name = "GPT-4o",
                providerId = "openai",
                contextWindow = "128k tokens",
                isDefault = false,
                supportsTools = true,
                supportsStreaming = true,
                description = "High-speed multimodal coding & tool-execution model."
            ),
            NavioModel(
                id = "claude-3-5-haiku",
                name = "Claude 3.5 Haiku",
                providerId = "anthropic",
                contextWindow = "200k tokens",
                isDefault = false,
                supportsTools = true,
                supportsStreaming = true,
                description = "Ultra-fast lightweight coding model for quick edits."
            ),
            NavioModel(
                id = "gpt-4o-mini",
                name = "GPT-4o Mini",
                providerId = "openai",
                contextWindow = "128k tokens",
                isDefault = false,
                supportsTools = true,
                supportsStreaming = true,
                description = "Cost-efficient compact model for simple scripts."
            ),
            NavioModel(
                id = "deepseek-chat",
                name = "DeepSeek V3",
                providerId = "openrouter",
                contextWindow = "64k tokens",
                isDefault = false,
                supportsTools = true,
                supportsStreaming = true,
                description = "OpenAI-compatible endpoint for DeepSeek coding capabilities."
            ),
            NavioModel(
                id = "deepseek-r1",
                name = "DeepSeek R1",
                providerId = "openrouter",
                contextWindow = "64k tokens",
                isDefault = false,
                supportsTools = true,
                supportsStreaming = true,
                description = "Reasoning model with deep verification chains."
            )
        )

        val initialProviders = listOf(
            NavioProvider(
                id = "anthropic",
                name = "Anthropic",
                protocol = "Messages API",
                baseUrl = "https://api.anthropic.com",
                isConfigured = true,
                maskedKey = "sk-ant-...489c",
                models = listOf("claude-3-7-sonnet", "claude-3-5-haiku")
            ),
            NavioProvider(
                id = "openai",
                name = "OpenAI",
                protocol = "OpenAI-compatible",
                baseUrl = "https://api.openai.com/v1",
                isConfigured = true,
                maskedKey = "sk-proj-...89df",
                models = listOf("gpt-4o", "gpt-4o-mini")
            ),
            NavioProvider(
                id = "openrouter",
                name = "OpenRouter",
                protocol = "OpenAI-compatible",
                baseUrl = "https://openrouter.ai/api/v1",
                isConfigured = true,
                maskedKey = "sk-or-...12a7",
                models = listOf("deepseek-chat", "deepseek-r1")
            )
        )

        _uiState.update {
            it.copy(
                sessions = initialSessions,
                models = initialModels,
                providers = initialProviders
            )
        }
    }

    fun onTabSelected(tab: NavTab) {
        _uiState.update { it.copy(currentTab = tab, selectedSession = null) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSelectSession(session: NavioSession?) {
        _uiState.update { it.copy(selectedSession = session) }
    }

    fun onSetShowNewSessionDialog(show: Boolean) {
        _uiState.update { it.copy(showNewSessionDialog = show) }
    }

    fun createSession(
        title: String,
        modelName: String,
        workspacePath: String,
        permissionMode: String
    ) {
        val newSession = NavioSession(
            id = "sess-${UUID.randomUUID().toString().take(6)}",
            title = title,
            lastActivity = "Just now",
            modelName = modelName,
            workspacePath = workspacePath,
            permissionMode = permissionMode,
            previewText = "// Ready for your first coding prompt",
            messageCount = 0,
            status = "Idle"
        )

        _uiState.update { current ->
            current.copy(
                sessions = listOf(newSession) + current.sessions,
                showNewSessionDialog = false,
                selectedSession = newSession // Open the newly created session directly
            )
        }
    }

    fun setDefaultModel(modelId: String) {
        _uiState.update { current ->
            current.copy(
                defaultModelId = modelId,
                models = current.models.map { it.copy(isDefault = it.id == modelId) }
            )
        }
    }

    fun addProvider(name: String, protocol: String, baseUrl: String, apiKey: String) {
        val masked = if (apiKey.length > 8) "${apiKey.take(4)}...${apiKey.takeLast(4)}" else "sk-...configured"
        val newProvider = NavioProvider(
            id = name.lowercase().replace(" ", "-"),
            name = name,
            protocol = protocol,
            baseUrl = baseUrl,
            isConfigured = apiKey.isNotEmpty(),
            maskedKey = masked,
            models = listOf("custom-model-1")
        )
        _uiState.update { current ->
            current.copy(providers = current.providers + newProvider)
        }
    }

    fun updatePermissionMode(mode: String) {
        _uiState.update { it.copy(defaultPermissionMode = mode) }
    }

    fun resetSampleData() {
        loadInitialData()
    }
}
