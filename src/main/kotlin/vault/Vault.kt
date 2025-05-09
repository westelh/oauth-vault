package dev.westelh.vault

import dev.westelh.vault.api.ErrorResponse
import dev.westelh.vault.api.identity.Identity
import dev.westelh.vault.api.kv.v2.Kv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class Vault(private val config: Config, private val client: HttpClient) {
    private val v1 = "${config.address}/v1"
    private val ui = "${config.address}/ui/vault"

    class VaultError(val response: HttpResponse, val body: ErrorResponse?) : Throwable(toString(response, body)) {
        companion object {
            private fun toString(response: HttpResponse, body: ErrorResponse?): String {
                val statusCode = response.status
                val method = response.request.method.value
                val at = response.request.url
                return if (body == null) {
                    "Vault client error: ${statusCode.description} at $method $at"
                } else {
                    "Vault client error${statusCode.description} at $method $at. Description: ${
                        body.errors.joinToString(
                            ", ", "[", "]"
                        )
                    }"
                }
            }
        }
    }

    private val configure: HttpRequestBuilder.() -> Unit = {
        // Authorizationヘッダが既に設定されている場合、上書きしない
        if (headers["Authorization"] == null) {
            if (config.token.isNotBlank()) bearerAuth(config.token)
        }
        contentType(ContentType.Application.Json)
    }

    suspend fun get(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse = client.get("$v1/$path") {
        block()
        configure()
    }

    suspend fun post(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse = client.post("$v1/$path") {
        block()
        configure()
    }

    suspend fun list(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        client.request("$v1/$path") {
            method = HttpMethod("LIST")
            block()
            configure()
        }

    suspend fun delete(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        client.delete("$v1/$path") {
            block()
            configure()
        }

    suspend fun patch(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse =
        client.patch("$v1/$path") {
            block()
            configure()
        }

    suspend fun put(path: String, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse = client.put("$v1/$path") {
        block()
        configure()
    }

    suspend inline fun <reified R> getOrVaultError(
        path: String, noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching {
        handleVaultResponse(get(path, block), HttpStatusCode.OK) { it.body() }
    }

    suspend inline fun <reified R> listOrVaultError(
        path: String, noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching {
        handleVaultResponse(list(path, block), HttpStatusCode.OK) { it.body() }
    }

    suspend inline fun <reified R> postOrVaultError(
        path: String, noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching {
        handleVaultResponse(post(path, block), listOf(HttpStatusCode.OK, HttpStatusCode.NoContent)) { it.body() }
    }

    suspend fun deleteOrVaultError(
        path: String, block: HttpRequestBuilder.() -> Unit = {}
    ): Result<Unit> = runCatching {
        handleVaultResponse(delete(path, block), HttpStatusCode.NoContent) { it.body() }
    }

    suspend inline fun <reified R> patchOrVaultError(
        path: String, noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching {
        handleVaultResponse(patch(path, block), listOf(HttpStatusCode.OK, HttpStatusCode.NoContent)) { it.body() }
    }

    suspend inline fun <reified R> putOrVaultError(
        path: String, noinline block: HttpRequestBuilder.() -> Unit = {}
    ): Result<Unit> = runCatching {
        handleVaultResponse(put(path, block), listOf(HttpStatusCode.OK, HttpStatusCode.NoContent)) { it.body() }
    }

    suspend fun <R> handleVaultResponse(
        response: HttpResponse, successStatus: List<HttpStatusCode>, transform: suspend (HttpResponse) -> R
    ): R {
        return if (successStatus.contains(response.status)) {
            transform(response)
        } else {
            if (response.contentType() == null) throw VaultError(response, null)
            else throw VaultError(response, response.body())
        }
    }

    suspend fun <R> handleVaultResponse(
        response: HttpResponse, successStatus: HttpStatusCode, transform: suspend (HttpResponse) -> R
    ): R = handleVaultResponse(
        response, listOf(successStatus), transform
    )

    fun oidcEndpointUrl(providerName: String): String {
        return "$ui/identity/oidc/provider/$providerName/authorize"
    }
}

fun Vault.kv(mount: String): Kv = Kv(this, mount)
fun Vault.identity(): Identity = Identity(this)