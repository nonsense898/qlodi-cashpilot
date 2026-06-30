package com.qlodi.cashpilot.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {
    val json: Json = Json {
        ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true; explicitNulls = false
    }

    fun create(tokenProvider: TokenProvider): HttpClient =
        HttpClient(apiHttpClientEngine()) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT_MS
                connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT_MS
            }
            install(Auth) {
                bearer {
                    loadTokens { tokenProvider.accessToken()?.let { BearerTokens(it, "") } }
                    refreshTokens { tokenProvider.refresh()?.let { BearerTokens(it, "") } }
                    sendWithoutRequest { req -> !req.url.toString().contains("/auth/") }
                }
            }
            defaultRequest { contentType(ContentType.Application.Json) }
            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, request ->
                    val response = (cause as? ResponseException)?.response
                        ?: return@handleResponseExceptionWithRequest
                    val ex = response.toApiException()
                    if (ex.status == 401 && !request.url.toString().contains("/auth/")) {
                        ApiConfig.onUnauthorized?.invoke()
                    }
                    throw ex
                }
            }
        }

    suspend fun HttpResponse.toApiException(): ApiException {
        val raw = runCatching { bodyAsText() }.getOrNull().orEmpty()
        val envelope = runCatching { json.decodeFromString<ApiErrorEnvelope>(raw) }.getOrNull()
        return ApiException(
            status = status.value,
            message = envelope?.error?.message?.ifBlank { status.description } ?: status.description,
            code = envelope?.error?.code,
        )
    }
}

suspend inline fun <reified T> apiCall(crossinline block: suspend () -> HttpResponse): ApiResult<T> =
    try {
        ApiResult.Ok(block().body())
    } catch (e: ApiException) {
        ApiResult.Err(e)
    } catch (e: Throwable) {
        ApiResult.Err(ApiException(status = 0, message = e.message ?: "Network error"))
    }
