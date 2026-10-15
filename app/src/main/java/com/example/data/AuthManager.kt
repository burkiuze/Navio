package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

data class NavioUser(val id: String, val email: String, val name: String, val avatarUrl: String?)

/**
 * Supabase Auth with Google, via the browser OAuth flow + PKCE (no SDK). The browser returns to
 * `navio://auth?code=…`, which [MainActivity] hands to [completeSignIn].
 */
class AuthManager private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("navio_auth", Context.MODE_PRIVATE)
    private val http = OkHttpClient()
    private val refreshLock = Mutex()

    private val _user = MutableStateFlow(readUser())
    val user: StateFlow<NavioUser?> = _user.asStateFlow()

    /** Browser URL that starts Google sign-in. */
    fun signInUrl(): Uri {
        val verifier = randomString()
        prefs.edit().putString(KEY_VERIFIER, verifier).apply()
        val challenge = Base64.encodeToString(
            MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        return Uri.parse("$SUPABASE_URL/auth/v1/authorize").buildUpon()
            .appendQueryParameter("provider", "google")
            .appendQueryParameter("redirect_to", REDIRECT_URI)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "s256")
            .build()
    }

    fun isAuthCallback(uri: Uri?): Boolean = uri?.scheme == "navio" && uri.host == "auth"

    /** Exchanges the `code` from the redirect for a session. Returns an error message or null. */
    suspend fun completeSignIn(uri: Uri): String? {
        uri.getQueryParameter("error_description")?.let { return it }
        val code = uri.getQueryParameter("code") ?: return "Giriş kodu alınamadı."
        val verifier = prefs.getString(KEY_VERIFIER, null) ?: return "Giriş oturumu bulunamadı, tekrar deneyin."
        return runCatching {
            val session = post("token?grant_type=pkce", JSONObject().put("auth_code", code).put("code_verifier", verifier))
            saveSession(session)
            prefs.edit().remove(KEY_VERIFIER).apply()
            null
        }.getOrElse { it.message ?: "Giriş başarısız." }
    }

    /** A valid access token, refreshed when it is about to expire; null when signed out. */
    suspend fun accessToken(): String? = refreshLock.withLock {
        val token = prefs.getString(KEY_ACCESS, null) ?: return null
        if (System.currentTimeMillis() / 1000 < prefs.getLong(KEY_EXPIRES, 0) - 60) return token
        val refresh = prefs.getString(KEY_REFRESH, null) ?: return token
        runCatching { saveSession(post("token?grant_type=refresh_token", JSONObject().put("refresh_token", refresh))) }
        prefs.getString(KEY_ACCESS, null)
    }

    fun signOut() {
        prefs.edit().clear().apply()
        _user.value = null
    }

    private suspend fun post(path: String, body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$SUPABASE_URL/auth/v1/$path")
            .header("apikey", SUPABASE_KEY)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
        http.newCall(request).execute().use { response ->
            val json = runCatching { JSONObject(response.body?.string().orEmpty()) }.getOrDefault(JSONObject())
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    json.optString("error_description").ifBlank { json.optString("msg").ifBlank { "Supabase hatası (${response.code})" } }
                )
            }
            json
        }
    }

    private fun saveSession(session: JSONObject) {
        val user = session.optJSONObject("user") ?: JSONObject()
        val meta = user.optJSONObject("user_metadata") ?: JSONObject()
        prefs.edit()
            .putString(KEY_ACCESS, session.optString("access_token"))
            .putString(KEY_REFRESH, session.optString("refresh_token"))
            .putLong(KEY_EXPIRES, session.optLong("expires_at", System.currentTimeMillis() / 1000 + session.optLong("expires_in", 3600)))
            .putString("user_id", user.optString("id"))
            .putString("email", user.optString("email"))
            .putString("name", meta.optString("full_name").ifBlank { meta.optString("name") })
            .putString("avatar", meta.optString("avatar_url").ifBlank { null })
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

    private fun randomString(): String {
        val bytes = ByteArray(48).also { SecureRandom().nextBytes(it) }
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    companion object {
        // Publishable (client-side) credentials of the "navio" Supabase project.
        const val SUPABASE_URL = "https://ihrxwdwglfqletaclwqu.supabase.co"
        const val SUPABASE_KEY = "sb_publishable_ec73PIdgezg2OXyHqTTQXg_yZ7UShwj"
        const val REDIRECT_URI = "navio://auth"

        private const val KEY_VERIFIER = "pkce_verifier"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EXPIRES = "expires_at"

        @Volatile
        private var instance: AuthManager? = null

        fun get(context: Context): AuthManager =
            instance ?: synchronized(this) {
                instance ?: AuthManager(context.applicationContext).also { instance = it }
            }
    }
}
