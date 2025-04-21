package dev.westelh.vault

import dev.westelh.vault.api.ErrorResponse
import dev.westelh.vault.api.identity.Identity
import dev.westelh.vault.api.kv.v2.Kv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class Vault(private val config: Config, engine: HttpClientEngine = Apache.create {  }) {
    private val v1 = "${config.address}/v1"

    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                allowTrailingComma = true
            })
        }
    }

    class VaultError(response: HttpResponse, body: ErrorResponse): Throwable(toString(response, body)) {
        companion object {
            private fun toString(response: HttpResponse, body: ErrorResponse): String {
                val statusCode = response.status
                val at = response.request.url
                return "$statusCode at ${at} - $body"
            }
        }
    }

    private val configure: HttpRequestBuilder.() -> Unit = {
        // トークンが空の時は、ベアラー認証を行わない
        if (config.token.isNotBlank()) { bearerAuth(config.token) }
        contentType(ContentType.Application.Json)
    }

    suspend fun get(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        client.get("$v1/$path") {
            configure()
            block()
        }

    suspend fun post(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        client.post("$v1/$path") {
            configure()
            block()
        }

    suspend fun delete(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        client.delete("$v1/$path") {
            configure()
            block()
        }

    suspend inline fun <reified R> getOrVaultError(
        path: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching {
        handleVaultResponse(get(path, block), HttpStatusCode.OK) { it.body() }
    }

    suspend inline fun <reified R> postOrVaultError(
        path: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching {
        handleVaultResponse(post(path, block), HttpStatusCode.OK) { it.body() }
    }

    suspend fun deleteOrVaultError(
        path: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): Result<Unit> = runCatching {
        handleVaultResponse(delete(path, block), HttpStatusCode.NoContent) { it.body() }
    }

    suspend fun <R> handleVaultResponse(response: HttpResponse, successStatus: HttpStatusCode, transform: suspend (HttpResponse) -> R): R {
        return if (response.status == successStatus) {
            transform(response)
        } else {
            throw VaultError(response, response.body())
        }
    }
}

fun Vault.kv(mount: String): Kv = Kv(this, mount)
fun Vault.identity(): Identity = Identity(this)