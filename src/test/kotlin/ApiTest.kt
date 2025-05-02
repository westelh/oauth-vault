package dev.westelh

import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test

class ApiTest {
    @Test
    fun testGetUserId() = testApplication {
        environment { config = MapApplicationConfig(
            "vault.addr" to "http://vault.example.com",
            "api.unsafe" to "true",
            "api.auth.jwt.audience" to "audience",
            "api.auth.jwt.issuer" to "issuer",
        ) }
        val testHttp = createClient { configure() }
        application { configureApi(testHttp) }

        val res = testHttp.get("/api/user/id")
        res.status shouldBe HttpStatusCode.OK
    }

    private fun <T : HttpClientEngineConfig> HttpClientConfig<T>.configure() {
        install(ContentNegotiation)
    }
}