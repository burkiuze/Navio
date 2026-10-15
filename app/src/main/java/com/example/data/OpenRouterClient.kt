package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenRouterException(message: String) : IOException(message)

/** One assistant turn as returned by OpenRouter's chat completions endpoint. */
data class CompletionResult(
    val message: JSONObject,
    val content: String,
    val toolCalls: List<ToolCall>,
    val sources: List<Source>
)

data class ToolCall(val id: String, val name: String, val arguments: JSONObject)

/**
 * Minimal OpenRouter client. Web search uses OpenRouter's `web` plugin, which grounds the answer in
 * live results and returns them as `url_citation` annotations.
 */
object OpenRouterClient {

    private const val ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"
    private const val WEB_RESULTS = 5
    private val JSON = "application/json".toMediaType()

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun complete(
        apiKey: String,
        model: String,
        messages: JSONArray,
        tools: JSONArray? = null,
        webSearch: Boolean = false
    ): CompletionResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw OpenRouterException("OpenRouter API anahtarı bulunamadı. Forge.env/.env dosyasını ekleyip yeniden derleyin ya da Ayarlar'dan anahtar girin.")
        }
        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
        if (tools != null && tools.length() > 0) body.put("tools", tools)
        if (webSearch) {
            body.put("plugins", JSONArray().put(JSONObject().put("id", "web").put("max_results", WEB_RESULTS)))
        }

        val request = Request.Builder()
            .url(ENDPOINT)
            .header("Authorization", "Bearer $apiKey")
            .header("HTTP-Referer", "https://github.com/burkiuze/Navio")
            .header("X-Title", "Navio")
            .post(body.toString().toRequestBody(JSON))
            .build()

        http.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            val json = runCatching { JSONObject(text) }.getOrNull()
            if (!response.isSuccessful || json == null || json.has("error")) {
                val error = json?.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
                throw OpenRouterException("OpenRouter hatası (${response.code}): ${error ?: text.take(300)}")
            }
            val message = json.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")
                ?: throw OpenRouterException("OpenRouter boş yanıt döndü.")
            CompletionResult(
                message = message,
                content = message.optString("content").takeUnless { message.isNull("content") }.orEmpty(),
                toolCalls = parseToolCalls(message.optJSONArray("tool_calls")),
                sources = parseSources(message.optJSONArray("annotations"))
            )
        }
    }

    private fun parseToolCalls(array: JSONArray?): List<ToolCall> {
        if (array == null) return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            val call = array.optJSONObject(i) ?: return@mapNotNull null
            val fn = call.optJSONObject("function") ?: return@mapNotNull null
            val args = runCatching { JSONObject(fn.optString("arguments", "{}")) }.getOrDefault(JSONObject())
            ToolCall(call.optString("id"), fn.optString("name"), args)
        }
    }

    private fun parseSources(array: JSONArray?): List<Source> {
        if (array == null) return emptyList()
        return (0 until array.length())
            .mapNotNull { array.optJSONObject(it)?.optJSONObject("url_citation") }
            .map { Source(title = it.optString("title").ifBlank { it.optString("url") }, url = it.optString("url")) }
            .filter { it.url.isNotBlank() }
            .distinctBy { it.url }
    }
}
