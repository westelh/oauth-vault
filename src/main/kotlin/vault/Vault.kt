package dev.westelh.vault

import dev.westelh.vault.api.VaultErrorResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class Vault(private val config: Config, engine: HttpClientEngine = Apache.create {  }) {
    val baseUrl: V1Path = Address(config.address).v1()

    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient(engine) {
        install(Logging) {
            level = LogLevel.BODY
        }
        install(ContentNegotiation) {
            json(Json {
                allowTrailingComma = true
            })
        }
    }

    class VaultError(cause: VaultErrorResponse?) : Throwable(cause?.toString().orEmpty())

    private val configure: HttpRequestBuilder.() -> Unit = {
        if (config.token.isNotBlank()) {
            headers { bearerAuth(config.token) }
        }

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

    suspend inline fun deleteOrVaultError(
        url: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<Unit> = runCatching {
        handleVaultResponse(delete(url, block), HttpStatusCode.NoContent) { it.body() }
    }

    suspend inline fun <R> handleVaultResponse(response: HttpResponse, successStatus: HttpStatusCode, transform: (HttpResponse) -> R): R {
        return if (response.status == successStatus) {
            transform(response)
        } else {
            if (response.bodyAsBytes().isEmpty()) throw VaultError(null)
            else throw VaultError(response.body())
        }
    }
}
