package dev.westelh

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json

private val jsonContent = headersOf(HttpHeaders.ContentType, "application/json")

fun createMockEngineRespondsWithErrors(error: List<String>, status: HttpStatusCode): MockEngine {
    return MockEngine { _ ->
        respond(
            content = Json.encodeToString(error),
            headers = jsonContent,
            status = status
        )
    }
}

fun createMockEngineNotAuthorized(): MockEngine {
    val error = listOf("permission denied")
    return createMockEngineRespondsWithErrors(error, HttpStatusCode.Forbidden)
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

fun createTestClient(client: HttpClient) = client.config {
    install(ContentNegotiation) {
        json()
    }
}