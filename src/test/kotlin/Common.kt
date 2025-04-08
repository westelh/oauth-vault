package dev.westelh

import dev.westelh.vault.api.VaultErrorResponse
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json

private val jsonContent = headersOf(HttpHeaders.ContentType, "application/json")

fun createMockEngineRespondsWithErrors(error: VaultErrorResponse, status: HttpStatusCode): MockEngine {
    return MockEngine { _ ->
        respond(
            content = Json.encodeToString(error),
            headers = jsonContent,
            status = status
        )
    }
}

fun createMockEngineNotAuthorized(): MockEngine {
    val content = VaultErrorResponse(listOf("permission denied"))
    return createMockEngineRespondsWithErrors(content, HttpStatusCode.Forbidden)
}

fun createMockEngineFromResource(resourcePath: String, status: HttpStatusCode = HttpStatusCode.OK): MockEngine {
    return MockEngine { _ ->
        respond(
            content = ByteReadChannel(this.javaClass.getResourceAsStream(resourcePath)!!.readAllBytes()),
            status = status,
            headers = jsonContent
        )
    }
}