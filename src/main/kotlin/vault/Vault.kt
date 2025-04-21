package dev.westelh.vault

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
    val baseUrl: V1Path = Address(config.address).v1()

    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                allowTrailingComma = true
            })
        }
    }

    class VaultError(statusCode: HttpStatusCode, message: String)
        : Throwable("Vault error: $statusCode - $message")

    private val configure: HttpRequestBuilder.() -> Unit = {
        // トークンが空の時は、ベアラー認証を行わない
        if (config.token.isNotBlank()) { bearerAuth(config.token) }
        contentType(ContentType.Application.Json)
    }

    suspend fun get(urlString: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        client.get(urlString) {
            configure()
            block()
        }

    suspend fun post(urlString: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        client.post(urlString) {
            configure()
            block()
        }

    suspend fun delete(urlString: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        client.delete(urlString) {
            configure()
            block()
        }

    suspend inline fun <reified R> getOrVaultError(
        url: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching {
        handleVaultResponse(get(url, block), HttpStatusCode.OK) { it.body() }
    }

    suspend inline fun <reified R> postOrVaultError(
        url: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching {
        handleVaultResponse(post(url, block), HttpStatusCode.OK) { it.body() }
    }

    suspend fun deleteOrVaultError(
        url: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): Result<Unit> = runCatching {
        handleVaultResponse(delete(url, block), HttpStatusCode.NoContent) { it.body() }
    }

    suspend fun <R> handleVaultResponse(response: HttpResponse, successStatus: HttpStatusCode, transform: suspend (HttpResponse) -> R): R {
        return if (response.status == successStatus) {
            transform(response)
        } else {
            if (response.bodyAsBytes().isEmpty()) {
                when (response.status) {
                    HttpStatusCode.Unauthorized -> throw VaultError(response.status, "Unauthorized")
                    HttpStatusCode.Forbidden -> throw VaultError(response.status, "Forbidden")
                    HttpStatusCode.NotFound -> throw VaultError(response.status, "Not Found")
                    else -> throw VaultError(response.status, "No description")
                }
            }
            else throw VaultError(response.body(), response.body<List<String>>().joinToString())
        }
    }
}
