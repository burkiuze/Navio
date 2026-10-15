package com.example.data

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Service endpoints, kept out of the source tree. Values come from Forge.env at build time
 * (BuildConfig) or, for builds without it, from the Vercel `navio-config` project at runtime
 * (cached so the app also starts offline).
 */
object RemoteConfig {

    private val http = OkHttpClient()
    private val lock = Mutex()
    @Volatile private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val clerkFrontendApi: String get() = BuildConfig.NAVIO_CLERK_FAPI.ifBlank { cached("clerkFrontendApi") }
    val aiProxyBase: String get() = BuildConfig.NAVIO_AI_PROXY.ifBlank { cached("aiProxyBase") }

    /** Makes sure the endpoints are known, fetching them from Vercel when needed. */
    suspend fun ensure() {
        if (clerkFrontendApi.isNotBlank() && aiProxyBase.isNotBlank()) {
            refreshQuietly()
            return
        }
        lock.withLock {
            if (clerkFrontendApi.isBlank() || aiProxyBase.isBlank()) fetch()
        }
        if (clerkFrontendApi.isBlank() || aiProxyBase.isBlank()) {
            throw IllegalStateException("Navio yapılandırması alınamadı. İnternet bağlantınızı kontrol edin.")
        }
    }

    /** Picks up changes made on Vercel once per process; cached values keep working meanwhile. */
    private suspend fun refreshQuietly() {
        if (refreshed || (BuildConfig.NAVIO_CLERK_FAPI.isNotBlank() && BuildConfig.NAVIO_AI_PROXY.isNotBlank())) return
        refreshed = true
        lock.withLock { fetch() }
    }

    @Volatile private var refreshed = false

    private suspend fun fetch() {
        val url = BuildConfig.NAVIO_CONFIG_URL.ifBlank { return }
        withContext(Dispatchers.IO) {
            runCatching {
                http.newCall(Request.Builder().url(url).build()).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val json = JSONObject(response.body?.string().orEmpty())
                    val editor = prefs()?.edit() ?: return@use
                    listOf("clerkFrontendApi", "aiProxyBase").forEach { key ->
                        json.optString(key).trimEnd('/').takeIf { it.isNotBlank() }?.let { editor.putString(key, it) }
                    }
                    editor.apply()
                }
            }
            refreshed = true
        }
    }

    private fun cached(key: String): String = prefs()?.getString(key, "").orEmpty()

    private fun prefs() = appContext?.getSharedPreferences("navio_remote_config", Context.MODE_PRIVATE)
}
