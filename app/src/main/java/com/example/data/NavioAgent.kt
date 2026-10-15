package com.example.data

import android.content.Context
import com.example.automation.AutomationScheduler
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AgentReply(
    val content: String,
    val sources: List<Source>,
    val createdAutomations: List<Automation>
)

/**
 * Talks to OpenRouter on behalf of the app. In chat it exposes a `create_automation` tool so the user
 * can set up Computer automations just by asking; automation runs reuse it without tools.
 */
class NavioAgent(context: Context) {

    private val appContext = context.applicationContext
    private val store = NavioStore.get(appContext)

    suspend fun reply(history: List<ChatMessage>, webSearch: Boolean, mode: AgentMode): AgentReply {
        val settings = store.settings.value
        val messages = JSONArray().put(system(systemPrompt(mode)))
        history.filterNot { it.isError }.forEach { msg ->
            messages.put(
                JSONObject()
                    .put("role", if (msg.role == Role.USER) "user" else "assistant")
                    .put("content", msg.content)
            )
        }

        val created = mutableListOf<Automation>()
        val sources = mutableListOf<Source>()
        var useWeb = webSearch
        repeat(MAX_TOOL_ROUNDS) {
            val result = OpenRouterClient.complete(
                apiKey = settings.effectiveApiKey,
                model = settings.model,
                messages = messages,
                tools = TOOLS,
                webSearch = useWeb
            )
            sources += result.sources
            if (result.toolCalls.isEmpty()) {
                return AgentReply(result.content.trim(), sources.distinctBy { it.url }, created)
            }
            messages.put(
                JSONObject()
                    .put("role", "assistant")
                    .put("content", result.content)
                    .put("tool_calls", result.message.optJSONArray("tool_calls"))
            )
            result.toolCalls.forEach { call ->
                val output = when (call.name) {
                    "create_automation" -> createAutomation(call.arguments).let { automation ->
                        created += automation
                        "Oluşturuldu: id=${automation.id}, ad=\"${automation.name}\", zamanlama=\"${automation.schedule.describe()}\""
                    }
                    "list_automations" -> listAutomations()
                    else -> "Bilinmeyen araç: ${call.name}"
                }
                messages.put(
                    JSONObject().put("role", "tool").put("tool_call_id", call.id).put("content", output)
                )
            }
            // Search results are already in context; don't pay for another web search on follow-ups.
            useWeb = false
        }
        val summary = created.joinToString("\n") { "• ${it.name} — ${it.schedule.describe()}" }
        return AgentReply(summary.ifBlank { "İşlem tamamlandı." }, sources.distinctBy { it.url }, created)
    }

    /** Executes one automation run: the stored prompt, with web search, no tools. */
    suspend fun runAutomation(automation: Automation): AutomationRun {
        val settings = store.settings.value
        val startedAt = System.currentTimeMillis()
        return try {
            val result = OpenRouterClient.complete(
                apiKey = settings.effectiveApiKey,
                model = settings.model,
                messages = JSONArray()
                    .put(system(automationRunPrompt(automation)))
                    .put(JSONObject().put("role", "user").put("content", automation.prompt)),
                webSearch = automation.useWebSearch
            )
            AutomationRun(UUID.randomUUID().toString(), startedAt, result.content.trim(), result.sources)
        } catch (e: Exception) {
            AutomationRun(UUID.randomUUID().toString(), startedAt, e.message ?: e.toString(), success = false)
        }
    }

    private fun createAutomation(args: JSONObject): Automation {
        val type = ScheduleType.from(args.optString("schedule_type"))
        val (hour, minute) = parseTime(args.optString("time"))
        val automation = Automation(
            id = UUID.randomUUID().toString(),
            name = args.optString("name").ifBlank { "Otomasyon" },
            prompt = args.optString("prompt").ifBlank { args.optString("name") },
            schedule = Schedule(
                type = type,
                intervalHours = args.optInt("interval_hours", 1).coerceIn(1, 168),
                hour = hour,
                minute = minute,
                weekday = args.optInt("weekday", 1).coerceIn(1, 7)
            ),
            useWebSearch = args.optBoolean("use_web_search", true)
        )
        store.upsertAutomation(automation)
        AutomationScheduler.schedule(appContext, automation)
        return automation
    }

    private fun listAutomations(): String {
        val all = store.automations.value
        if (all.isEmpty()) return "Henüz otomasyon yok."
        return all.joinToString("\n") {
            "- ${it.name} (${it.schedule.describe()}, ${if (it.enabled) "aktif" else "duraklatıldı"}): ${it.prompt}"
        }
    }

    private fun parseTime(value: String): Pair<Int, Int> {
        val parts = value.trim().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return hour to minute
    }

    private fun now(): String =
        SimpleDateFormat("d MMMM yyyy EEEE, HH:mm", Locale("tr")).format(Date())

    private fun systemPrompt(mode: AgentMode): String = buildString {
        appendLine("Sen Navio'sun: web araması yapabilen, kullanıcının diliyle yanıt veren yardımcı bir yapay zekâ asistanısın.")
        appendLine("Şu an: ${now()}.")
        appendLine("Web sonuçları verildiyse onlara dayan ve iddiaları kaynaklarla destekle. Yanıtları net ve Markdown ile biçimlendir.")
        appendLine("Kullanıcı tekrarlanan ya da zamanlanmış bir iş istediğinde (ör. \"her sabah 9'da haberleri özetle\") create_automation aracıyla Computer otomasyonu kur.")
        appendLine("Zaman belirtilmemişse makul bir varsayılan seç ve yanıtında belirt. Otomasyonun prompt alanı, her çalıştırmada tek başına anlaşılacak kadar ayrıntılı olmalı.")
        if (mode == AgentMode.COMPUTER) {
            appendLine("Kullanıcı şu an Computer modunda: mesajı büyük ihtimalle kurulacak bir otomasyon tarifidir. Uygunsa doğrudan otomasyonu oluştur, sonra kısaca ne kurduğunu özetle.")
        }
    }

    private fun automationRunPrompt(automation: Automation) = buildString {
        appendLine("Sen Navio Computer'sın ve \"${automation.name}\" adlı zamanlanmış otomasyonu çalıştırıyorsun.")
        appendLine("Şu an: ${now()}.")
        appendLine("Görevi eksiksiz yerine getir ve sonucu kullanıcıya bildirim/rapor olarak okunacak şekilde, Markdown ile, kullanıcının dilinde yaz. Soru sorma.")
    }

    private fun system(content: String) = JSONObject().put("role", "system").put("content", content)

    companion object {
        private const val MAX_TOOL_ROUNDS = 4

        private val TOOLS: JSONArray = JSONArray().put(
            function(
                name = "create_automation",
                description = "Kullanıcı için zamanlanmış bir Computer otomasyonu oluşturur. Otomasyon her çalıştığında prompt web araması ile yürütülür ve sonucu bildirim olarak gönderilir.",
                properties = JSONObject()
                    .put("name", prop("string", "Kısa, açıklayıcı ad (ör. \"Sektör haberleri özeti\")."))
                    .put("prompt", prop("string", "Her çalıştırmada yürütülecek ayrıntılı talimat."))
                    .put(
                        "schedule_type",
                        prop("string", "hourly: her N saatte; daily: her gün belirli saatte; weekly: haftada bir; manual: yalnızca elle.")
                            .put("enum", JSONArray(listOf("hourly", "daily", "weekly", "manual")))
                    )
                    .put("interval_hours", prop("integer", "hourly için kaç saatte bir (1-168)."))
                    .put("time", prop("string", "daily/weekly için 24 saat biçiminde HH:mm."))
                    .put("weekday", prop("integer", "weekly için gün: 1=Pazartesi … 7=Pazar."))
                    .put("use_web_search", prop("boolean", "Çalışırken web araması kullanılsın mı (varsayılan true).")),
                required = listOf("name", "prompt", "schedule_type")
            )
        ).put(
            function(
                name = "list_automations",
                description = "Kullanıcının mevcut Computer otomasyonlarını listeler.",
                properties = JSONObject(),
                required = emptyList()
            )
        )

        private fun prop(type: String, description: String) =
            JSONObject().put("type", type).put("description", description)

        private fun function(name: String, description: String, properties: JSONObject, required: List<String>) =
            JSONObject().put("type", "function").put(
                "function",
                JSONObject()
                    .put("name", name)
                    .put("description", description)
                    .put(
                        "parameters",
                        JSONObject().put("type", "object").put("properties", properties).put("required", JSONArray(required))
                    )
            )
    }
}

enum class AgentMode { CHAT, COMPUTER }
