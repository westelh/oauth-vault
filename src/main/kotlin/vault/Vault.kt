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

class Vault(private val config: VaultConfig, engine: HttpClientEngine = Apache.create {  }) {
    val baseUrl: V1Path = Address(config.address).v1()

    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient(engine) {
        install(Logging) {
            level = LogLevel.INFO
        }
        install(ContentNegotiation) {
            json(Json {
                allowTrailingComma = true
            })
        }
    }


    class VaultError(cause: VaultErrorResponse?) : Throwable(cause?.toString().orEmpty())

    private val configure: HttpRequestBuilder.() -> Unit = {
        if (config.token.isBlank()) {
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

    suspend inline fun <reified R> readAndGetResult(
        url: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching {
        get(url, block)
    }.mapCatching { response ->
        if (response.status == HttpStatusCode.OK) response.body<R>()
        else {
            if (response.bodyAsBytes().isEmpty()) throw VaultError(null)
            else throw VaultError(response.body())
        }
    }

    suspend inline fun <reified R> writeAndGetResult(
        url: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching {
        post(url, block)
    }.mapCatching { response ->
        if (response.status == HttpStatusCode.OK) response.body<R>()
        else throw VaultError(response.body())
    }

    suspend inline fun deleteAndGetResult(
        url: String,
        noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<Unit> = runCatching {
        delete(url, block)
    }.mapCatching { response ->
        if (response.status == HttpStatusCode.NoContent) response.body<Unit>()
        else throw VaultError(response.body())
    }
}

interface VaultConfig {
    val address: String
    val token: String
}