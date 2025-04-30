package dev.westelh

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            root()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
