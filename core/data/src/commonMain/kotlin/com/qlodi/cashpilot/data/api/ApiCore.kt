package com.qlodi.cashpilot.data.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Конфіг REST-бекенду (спільний qlodi-backend). */
object ApiConfig {
    var baseUrl: String = "https://api.qlodi.app"
    const val API_PREFIX: String = "/v1"
    const val REQUEST_TIMEOUT_MS: Long = 30_000
    const val CONNECT_TIMEOUT_MS: Long = 15_000
    var onUnauthorized: (() -> Unit)? = null

    fun url(path: String): String {
        val p = if (path.startsWith("/")) path else "/$path"
        return "$baseUrl$API_PREFIX$p"
    }
}

@Serializable
data class ApiErrorEnvelope(val error: ApiError? = null)

@Serializable
data class ApiError(val code: String? = null, val message: String = "")

class ApiException(
    val status: Int,
    override val message: String,
    val code: String? = null,
) : RuntimeException(message)

sealed class ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>()
    data class Err(val error: ApiException) : ApiResult<Nothing>()

    fun getOrNull(): T? = (this as? Ok)?.value
}

/** Постачальник токена для bearer-auth + refresh. */
interface TokenProvider {
    suspend fun accessToken(): String?
    suspend fun refresh(): String?
}

/** Проста in-memory сесія (idToken + refreshToken). */
object SessionStore : TokenProvider {
    var idToken: String? = null
    var refreshToken: String? = null
    var uid: String? = null
    var email: String? = null

    /** Колбек оновлення токена через /auth/refresh (виставляється з ApiClient-шару). */
    var refresher: (suspend (String) -> Pair<String, String>?)? = null

    private val persistJson = Json { ignoreUnknownKeys = true }

    // Відновлюємо збережену сесію на старті: інакше перезавантаження сторінки
    // стирало in-memory токен і викидало залогінену людину на екран входу.
    // init виконується під час першого доступу до SessionStore — раніше, ніж
    // AppState читає isLoggedIn.
    init { restore() }

    fun set(idToken: String, refreshToken: String, uid: String, email: String) {
        this.idToken = idToken; this.refreshToken = refreshToken; this.uid = uid; this.email = email
        persist()
    }

    fun clear() {
        idToken = null; refreshToken = null; uid = null; email = null
        persist()
    }

    val isLoggedIn: Boolean get() = idToken != null

    override suspend fun accessToken(): String? = idToken
    override suspend fun refresh(): String? {
        val rt = refreshToken ?: return null
        val pair = refresher?.invoke(rt) ?: return null
        idToken = pair.first; refreshToken = pair.second
        persist()
        return idToken
    }

    /** Зберегти поточну сесію у платформене сховище (localStorage на вебі). */
    private fun persist() {
        val t = idToken; val rt = refreshToken; val u = uid; val e = email
        saveSessionJson(
            if (t != null && rt != null && u != null && e != null)
                persistJson.encodeToString(UserSession(uid = u, email = e, idToken = t, refreshToken = rt))
            else null,
        )
    }

    private fun restore() {
        val raw = loadSessionJson() ?: return
        val s = runCatching { persistJson.decodeFromString<UserSession>(raw) }.getOrNull() ?: return
        idToken = s.idToken; refreshToken = s.refreshToken; uid = s.uid; email = s.email
    }
}
