package com.example.data

import com.example.BuildConfig
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
 * OpenAI-compatible chat client. Requests go to the Forge AI proxy on Vercel (URL from
 * [RemoteConfig]), which holds the
 * OpenRouter key server-side, so users never enter a key. If a key was baked in from Forge.env at
 * build time, OpenRouter is used directly as a fallback when the proxy is unreachable.
 * Web search uses OpenRouter's `web` plugin; results come back as `url_citation` annotations.
 */
object OpenRouterClient {

    private const val OPENROUTER_BASE = "https://openrouter.ai/api/v1"
    private const val WEB_RESULTS = 5
    private val JSON = "application/json".toMediaType()

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private class HttpFailure(val code: Int, message: String) : IOException(message)

    suspend fun complete(
        model: String,
        messages: JSONArray,
        tools: JSONArray? = null,
        webSearch: Boolean = false,
        userToken: String? = null
    ): CompletionResult = withContext(Dispatchers.IO) {
        RemoteConfig.ensure()
        try {
            completeWithFallbacks(model, messages, tools, webSearch, userToken)
        } catch (e: HttpFailure) {
            throw OpenRouterException(e.message ?: "Yapay zekâ isteği başarısız oldu.")
        }
    }

    /** Degrades gracefully: drops web search, then tools, if the upstream rejects them. */
    private fun completeWithFallbacks(
        model: String,
        messages: JSONArray,
        tools: JSONArray?,
        webSearch: Boolean,
        userToken: String?
    ): CompletionResult {
        val attempts = buildList {
            add(webSearch to tools)
            if (webSearch) add(false to tools)
            if (tools != null) add(false to null)
        }.distinct()
        var last: HttpFailure? = null
        for ((web, t) in attempts) {
            try {
                return send(body(model, messages, t, web), userToken)
            } catch (e: HttpFailure) {
                last = e
                if (e.code in 500..599 || e.code == 401 || e.code == 429) break
            }
        }
        throw last ?: HttpFailure(0, "Yapay zekâ isteği başarısız oldu.")
    }

    private fun body(model: String, messages: JSONArray, tools: JSONArray?, webSearch: Boolean): JSONObject {
        val body = JSONObject().put("model", model).put("messages", messages)
        if (tools != null && tools.length() > 0) body.put("tools", tools)
        if (webSearch) {
            body.put("plugins", JSONArray().put(JSONObject().put("id", "web").put("max_results", WEB_RESULTS)))
        }
        return body
    }

    private fun send(body: JSONObject, userToken: String?): CompletionResult {
        val directKey = BuildConfig.OPENROUTER_KEY
        return try {
            post("${RemoteConfig.aiProxyBase}/chat/completions", body, userToken)
        } catch (e: IOException) {
            val proxyDown = e !is HttpFailure || e.code >= 500 || e.code == 404
            if (directKey.isBlank() || !proxyDown) throw e
            post("$OPENROUTER_BASE/chat/completions", body, directKey)
        }
    }

    private fun post(url: String, body: JSONObject, bearer: String?): CompletionResult {
        val request = Request.Builder()
            .url(url)
            .apply { if (!bearer.isNullOrBlank()) header("Authorization", "Bearer $bearer") }
            .header("x-forge-client", "navio")
            .header("HTTP-Referer", "https://github.com/burkiuze/Navio")
            .header("X-Title", "Navio")
            .post(body.toString().toRequestBody(JSON))
            .build()

        http.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            val json = runCatching { JSONObject(text) }.getOrNull()
            if (!response.isSuccessful || json == null || json.has("error")) {
                val error = json?.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
                throw HttpFailure(response.code, "Yapay zekâ hatası (${response.code}): ${error ?: text.take(300)}")
            }
            val message = json.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")
                ?: throw HttpFailure(502, "Yapay zekâ boş yanıt döndü.")
            return CompletionResult(
                message = message,
                content = message.optString("content").takeUnless { message.isNull("content") }.orEmpty(),
                toolCalls = parseToolCalls(message.optJSONArray("tool_calls")),
                sources = parseSources(message.optJSONArray("annotations"))
            )
        }
    }

    /** Free models the proxy serves, for the model picker. */
    suspend fun listModels(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            RemoteConfig.ensure()
            http.newCall(Request.Builder().url("${RemoteConfig.aiProxyBase}/models").build()).execute().use { response ->
                val data = JSONObject(response.body?.string().orEmpty()).optJSONArray("data") ?: JSONArray()
                (0 until data.length()).mapNotNull { data.optJSONObject(it) }
                    .map { it.optString("id") to it.optString("name").ifBlank { it.optString("id") } }
                    .filter { it.first.isNotBlank() }
            }
        }.getOrDefault(emptyList())
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
