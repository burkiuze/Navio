package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Process-wide store for conversations, automations and settings, persisted as JSON in the app's
 * private files dir. Shared by the UI and by [com.example.automation.AutomationWorker].
 */
class NavioStore private constructor(context: Context) {

    private val file = File(context.filesDir, "navio.json")
    private val prefs = context.getSharedPreferences("navio_settings", Context.MODE_PRIVATE)

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _automations = MutableStateFlow<List<Automation>>(emptyList())
    val automations: StateFlow<List<Automation>> = _automations.asStateFlow()

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    init {
        RemoteConfig.init(context)
        load()
    }

    // region settings

    data class Settings(val model: String, val webSearch: Boolean)

    private fun readSettings(): Settings {
        val saved = prefs.getString("model", null)
        // Earlier builds only had the Forge proxy's free models; move those users to Auto.
        val model = saved?.takeUnless { it.endsWith(":free") || it == "openrouter/free" } ?: DEFAULT_MODEL
        return Settings(model = model, webSearch = prefs.getBoolean("web_search", true))
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        val next = transform(_settings.value)
        prefs.edit()
            .putString("model", next.model.trim().ifBlank { DEFAULT_MODEL })
            .putBoolean("web_search", next.webSearch)
            .apply()
        _settings.value = readSettings()
    }

    // endregion

    // region conversations

    fun conversation(id: String): Conversation? = _conversations.value.firstOrNull { it.id == id }

    fun upsertConversation(conversation: Conversation) {
        _conversations.update { list ->
            listOf(conversation) + list.filterNot { it.id == conversation.id }
        }
        save()
    }

    fun deleteConversation(id: String) {
        _conversations.update { list -> list.filterNot { it.id == id } }
        save()
    }

    // endregion

    // region automations

    fun automation(id: String): Automation? = _automations.value.firstOrNull { it.id == id }

    fun upsertAutomation(automation: Automation) {
        _automations.update { list ->
            if (list.any { it.id == automation.id }) list.map { if (it.id == automation.id) automation else it }
            else listOf(automation) + list
        }
        save()
    }

    fun addRun(automationId: String, run: AutomationRun) {
        _automations.update { list ->
            list.map { if (it.id == automationId) it.copy(runs = (listOf(run) + it.runs).take(MAX_RUNS)) else it }
        }
        save()
    }

    fun deleteAutomation(id: String) {
        _automations.update { list -> list.filterNot { it.id == id } }
        save()
    }

    // endregion

    // region persistence

    @Synchronized
    private fun save() {
        val root = JSONObject()
            .put("conversations", JSONArray(_conversations.value.map { it.toJson() }))
            .put("automations", JSONArray(_automations.value.map { it.toJson() }))
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(root.toString())
        tmp.renameTo(file)
    }

    private fun load() {
        if (!file.exists()) return
        runCatching {
            val root = JSONObject(file.readText())
            _conversations.value = root.optJSONArray("conversations").objects().map { it.toConversation() }
            _automations.value = root.optJSONArray("automations").objects().map { it.toAutomation() }
        }
    }

    // endregion

    companion object {
        const val DEFAULT_MODEL = "openrouter/auto"
        private const val MAX_RUNS = 20

        @Volatile
        private var instance: NavioStore? = null

        fun get(context: Context): NavioStore =
            instance ?: synchronized(this) {
                instance ?: NavioStore(context.applicationContext).also { instance = it }
            }
    }
}

// region JSON mapping

private fun JSONArray?.objects(): List<JSONObject> =
    if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }

private fun JSONArray?.strings(): List<String> =
    if (this == null) emptyList() else (0 until length()).map { optString(it) }

private fun Source.toJson() = JSONObject().put("title", title).put("url", url)
private fun JSONObject.toSource() = Source(optString("title"), optString("url"))

private fun ChatMessage.toJson() = JSONObject()
    .put("id", id)
    .put("role", role.name)
    .put("content", content)
    .put("sources", JSONArray(sources.map { it.toJson() }))
    .put("automations", JSONArray(createdAutomationIds))
    .put("error", isError)
    .put("createdAt", createdAt)

private fun JSONObject.toMessage() = ChatMessage(
    id = optString("id"),
    role = runCatching { Role.valueOf(optString("role")) }.getOrDefault(Role.USER),
    content = optString("content"),
    sources = optJSONArray("sources").objects().map { it.toSource() },
    createdAutomationIds = optJSONArray("automations").strings(),
    isError = optBoolean("error"),
    createdAt = optLong("createdAt")
)

private fun Conversation.toJson() = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("updatedAt", updatedAt)
    .put("messages", JSONArray(messages.map { it.toJson() }))

private fun JSONObject.toConversation() = Conversation(
    id = optString("id"),
    title = optString("title"),
    updatedAt = optLong("updatedAt"),
    messages = optJSONArray("messages").objects().map { it.toMessage() }
)

private fun Schedule.toJson() = JSONObject()
    .put("type", type.id)
    .put("intervalHours", intervalHours)
    .put("hour", hour)
    .put("minute", minute)
    .put("weekday", weekday)

private fun JSONObject.toSchedule() = Schedule(
    type = ScheduleType.from(optString("type")),
    intervalHours = optInt("intervalHours", 1),
    hour = optInt("hour", 9),
    minute = optInt("minute", 0),
    weekday = optInt("weekday", 1)
)

private fun AutomationRun.toJson() = JSONObject()
    .put("id", id)
    .put("startedAt", startedAt)
    .put("output", output)
    .put("success", success)
    .put("sources", JSONArray(sources.map { it.toJson() }))

private fun JSONObject.toRun() = AutomationRun(
    id = optString("id"),
    startedAt = optLong("startedAt"),
    output = optString("output"),
    success = optBoolean("success", true),
    sources = optJSONArray("sources").objects().map { it.toSource() }
)

private fun Automation.toJson() = JSONObject()
    .put("id", id)
    .put("name", name)
    .put("prompt", prompt)
    .put("schedule", schedule.toJson())
    .put("web", useWebSearch)
    .put("enabled", enabled)
    .put("createdAt", createdAt)
    .put("runs", JSONArray(runs.map { it.toJson() }))

private fun JSONObject.toAutomation() = Automation(
    id = optString("id"),
    name = optString("name"),
    prompt = optString("prompt"),
    schedule = optJSONObject("schedule")?.toSchedule() ?: Schedule(ScheduleType.MANUAL),
    useWebSearch = optBoolean("web", true),
    enabled = optBoolean("enabled", true),
    createdAt = optLong("createdAt"),
    runs = optJSONArray("runs").objects().map { it.toRun() }
)

// endregion
