package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class NavioUser(val id: String, val email: String, val name: String, val avatarUrl: String?)

enum class AuthProvider(val strategy: String, val label: String) {
    GOOGLE("oauth_google", "Google"),
    GITHUB("oauth_github", "GitHub"),
    GITLAB("oauth_gitlab", "GitLab")
}

class AuthException(message: String) : Exception(message)

/**
 * Clerk sign-in over the Frontend API in native mode (no SDK — the official one needs a newer
 * Kotlin/Compose than this project). Mirrors clerk-android's redirect SSO flow:
 * create sign-in → open the provider in the browser → Clerk redirects to [redirectUrl] with a
 * `rotating_token_nonce` → reload the sign-in (or transfer to a sign-up for new users).
 * The device token Clerk returns in the `Authorization` header identifies this client.
 */
class AuthManager private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("navio_clerk", Context.MODE_PRIVATE)
    private val http = OkHttpClient()
    private val redirectUrl = "clerk://${context.packageName}.callback"

    init {
        RemoteConfig.init(context)
    }

    private val _user = MutableStateFlow(readUser())
    val user: StateFlow<NavioUser?> = _user.asStateFlow()

    /** Starts OAuth with [provider] and returns the provider URL to open in the browser. */
    suspend fun startSignIn(provider: AuthProvider): Uri {
        val created = call("POST", "client/sign_ins", mapOf("strategy" to provider.strategy, "redirect_url" to redirectUrl))
        var signIn = created.optJSONObject("response") ?: throw AuthException("Clerk giriş başlatılamadı.")
        var external = externalUrl(signIn)
        if (external == null) {
            signIn = call(
                "POST",
                "client/sign_ins/${signIn.optString("id")}/prepare_first_factor",
                mapOf("strategy" to provider.strategy, "redirect_url" to redirectUrl)
            ).optJSONObject("response") ?: signIn
            external = externalUrl(signIn)
        }
        prefs.edit().putString(KEY_SIGN_IN, signIn.optString("id")).apply()
        return Uri.parse(external ?: throw AuthException("Clerk yönlendirme adresi döndürmedi."))
    }

    fun isAuthCallback(uri: Uri?): Boolean =
        uri != null && uri.scheme == "clerk" && uri.host == redirectUrl.substringAfter("://")

    /** Finishes sign-in from the browser callback. Returns an error message, or null on success. */
    suspend fun completeSignIn(uri: Uri): String? = runCatching {
        uri.getQueryParameter("error_description")?.let { throw AuthException(it) }
        uri.getQueryParameter("error")?.let { throw AuthException("Giriş iptal edildi ($it).") }
        val signInId = prefs.getString(KEY_SIGN_IN, null) ?: throw AuthException("Giriş oturumu bulunamadı, tekrar deneyin.")
        val nonce = uri.getQueryParameter("rotating_token_nonce")?.takeIf { it.isNotBlank() }

        var result = call("GET", "client/sign_ins/$signInId", query = nonce?.let { mapOf("rotating_token_nonce" to it) }.orEmpty())
        val signIn = result.optJSONObject("response")
        val hasSession = (result.optJSONObject("client")?.optJSONArray("sessions")?.length() ?: 0) > 0
        val transferable = signIn?.optJSONObject("first_factor_verification")?.optString("status") == "transferable"
        val signedIn = hasSession || signIn?.optString("status") == "complete"
        if (!signedIn && (transferable || uri.getQueryParameter("__clerk_status") == "transfer")) {
            // No Clerk user for this external account yet: turn it into a sign-up.
            result = call("POST", "client/sign_ups", mapOf("transfer" to "true"))
            val signUp = result.optJSONObject("response")
            if (signUp?.optString("status") != "complete") {
                val missing = signUp?.optJSONArray("missing_fields")?.join(", ").orEmpty()
                throw AuthException("Kayıt tamamlanamadı${if (missing.isNotBlank()) " (eksik: $missing)" else ""}.")
            }
        } else if (!signedIn) {
            throw AuthException("Giriş tamamlanamadı (${signIn?.optString("status")}).")
        }
        val client = result.optJSONObject("client") ?: call("GET", "client").optJSONObject("response")
        saveUser(client ?: throw AuthException("Clerk oturumu alınamadı."))
        prefs.edit().remove(KEY_SIGN_IN).apply()
        null
    }.getOrElse { it.message ?: "Giriş başarısız." }

    /** Short-lived session JWT for calling our backend; null when signed out or offline. */
    suspend fun accessToken(): String? {
        val sessionId = prefs.getString(KEY_SESSION, null) ?: return null
        return runCatching { call("POST", "client/sessions/$sessionId/tokens", emptyMap()).optString("jwt").ifBlank { null } }
            .getOrNull()
    }

    suspend fun signOut() {
        prefs.getString(KEY_SESSION, null)?.let { id ->
            runCatching { call("POST", "client/sessions/$id/remove", emptyMap()) }
        }
        val deviceId = prefs.getString(KEY_DEVICE, null)
        prefs.edit().clear().apply()
        deviceId?.let { prefs.edit().putString(KEY_DEVICE, it).apply() }
        _user.value = null
    }

    private fun externalUrl(signIn: JSONObject): String? =
        signIn.optJSONObject("first_factor_verification")
            ?.optString("external_verification_redirect_url")
            ?.takeIf { it.isNotBlank() && it != "null" }

    private fun saveUser(client: JSONObject) {
        val sessions = client.optJSONArray("sessions") ?: JSONArray()
        val activeId = client.optString("last_active_session_id")
        val session = (0 until sessions.length()).mapNotNull { sessions.optJSONObject(it) }
            .firstOrNull { it.optString("id") == activeId } ?: sessions.optJSONObject(0)
            ?: throw AuthException("Aktif oturum bulunamadı.")
        val user = session.optJSONObject("user") ?: JSONObject()
        val emails = user.optJSONArray("email_addresses") ?: JSONArray()
        val primaryEmailId = user.optString("primary_email_address_id")
        val email = (0 until emails.length()).mapNotNull { emails.optJSONObject(it) }
            .let { list -> list.firstOrNull { it.optString("id") == primaryEmailId } ?: list.firstOrNull() }
            ?.optString("email_address").orEmpty()
        val name = listOf(user.optString("first_name"), user.optString("last_name"))
            .filter { it.isNotBlank() && it != "null" }.joinToString(" ")
            .ifBlank { user.optString("username").takeIf { it.isNotBlank() && it != "null" } ?: email.substringBefore('@') }
        prefs.edit()
            .putString(KEY_SESSION, session.optString("id"))
            .putString("user_id", user.optString("id"))
            .putString("email", email)
            .putString("name", name)
            .putString("avatar", user.optString("image_url").takeIf { it.isNotBlank() && it != "null" })
            .apply()
        _user.value = readUser()
    }

    private fun readUser(): NavioUser? {
        val id = prefs.getString("user_id", null)?.takeIf { it.isNotBlank() } ?: return null
        val email = prefs.getString("email", "").orEmpty()
        return NavioUser(
            id = id,
            email = email,
            name = prefs.getString("name", null)?.takeIf { it.isNotBlank() } ?: email.substringBefore('@'),
            avatarUrl = prefs.getString("avatar", null)
        )
    }

    private fun deviceId(): String =
        prefs.getString(KEY_DEVICE, null) ?: UUID.randomUUID().toString().also { prefs.edit().putString(KEY_DEVICE, it).apply() }

    /** One Frontend API call; `form` = null sends no body. Returns the parsed JSON body. */
    private suspend fun call(
        method: String,
        path: String,
        form: Map<String, String>? = null,
        query: Map<String, String> = emptyMap()
    ): JSONObject {
        RemoteConfig.ensure()
        return callFapi(method, path, form, query)
    }

    private suspend fun callFapi(
        method: String,
        path: String,
        form: Map<String, String>?,
        query: Map<String, String>
    ): JSONObject = withContext(Dispatchers.IO) {
        val url = "${RemoteConfig.clerkFrontendApi}/v1/$path".toHttpUrl().newBuilder()
            .addQueryParameter("_is_native", "true")
            .apply { query.forEach { (k, v) -> addQueryParameter(k, v) } }
            .build()
        val body = form?.let { fields -> FormBody.Builder().apply { fields.forEach { (k, v) -> add(k, v) } }.build() }
        val request = Request.Builder()
            .url(url)
            .method(method, if (method == "GET") null else (body ?: FormBody.Builder().build()))
            .header("clerk-api-version", API_VERSION)
            .header("x-mobile", "1")
            .header("x-native-device-id", deviceId())
            .apply { prefs.getString(KEY_DEVICE_TOKEN, null)?.let { header("Authorization", it) } }
            .build()
        http.newCall(request).execute().use { response ->
            response.header("Authorization")?.takeIf { it.isNotBlank() }?.let {
                prefs.edit().putString(KEY_DEVICE_TOKEN, it).apply()
            }
            val json = runCatching { JSONObject(response.body?.string().orEmpty()) }.getOrDefault(JSONObject())
            if (!response.isSuccessful) {
                val error = json.optJSONArray("errors")?.optJSONObject(0)
                throw AuthException(
                    error?.optString("long_message")?.takeIf { it.isNotBlank() }
                        ?: error?.optString("message")?.takeIf { it.isNotBlank() }
                        ?: "Clerk hatası (${response.code})"
                )
            }
            json
        }
    }

    companion object {
        private const val API_VERSION = "2026-08-20"

        private const val KEY_DEVICE = "device_id"
        private const val KEY_DEVICE_TOKEN = "device_token"
        private const val KEY_SIGN_IN = "pending_sign_in"
        private const val KEY_SESSION = "session_id"

        @Volatile
        private var instance: AuthManager? = null

        fun get(context: Context): AuthManager =
            instance ?: synchronized(this) {
                instance ?: AuthManager(context.applicationContext).also { instance = it }
            }
    }
}
