package dev.westelh

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.server.testing.testApplication
import kotlin.test.Test

class ApiTest {
    @Test
    fun testGetUserId() = testApplication {
        val testHttp = createClient { configure() }
        application { configureApi(testHttp) }

        val res = testHttp.get("/api/user/id")
        assert(res.status.value == 200)
    }

    private fun <T : HttpClientEngineConfig> HttpClientConfig<T>.configure() {
        install(ContentNegotiation)
    }
}