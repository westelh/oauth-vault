package dev.westelh

import dev.westelh.model.GoogleIdentityData
import dev.westelh.model.OAuthCodes
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlin.test.Test

class UserPageTest {
    @Serializable
    data class TestUserInfo(
        val google: GoogleIdentityData
    )

    @Test
    fun testLogin() = testApplication {
        environment {
            config = MapApplicationConfig(
                "vault.addr" to "http://vault.example.com",
                "vault.kv" to "kv",
                "vault.oauth.provider" to "default",
            )
        }
        val testHttp = createClient {
            install(HttpCookies)
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }
        application {
            configureUserPage(testHttp)
        }
        routing {
            get("/test-login") {
                call.sessions.set(OAuthCodes(
                    accessToken = "123456",
                    tokenType = "Bearer",
                    expiresIn = 3600,
                    refreshToken = "456789",
                    extraParameters = emptyMap(),
                    state = "test-state"
                ))
            }
        }
        externalServices {
            hosts("http://vault.example.com") {
                install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                    json()
                }
                routing {
                    get("/v1/identity/oidc/provider/default/userinfo") {
                        call.request.headers["Authorization"].shouldBe("Bearer 123456")
                        call.respond(TestUserInfo(GoogleIdentityData("xyz123")))
                    }
                }
            }
        }

        val loginRes = testHttp.get("/test-login")
        val summaryRes =  testHttp.get("/user/summary")
        summaryRes.status shouldBe HttpStatusCode.OK
        summaryRes.bodyAsText().shouldContain("xyz123")
    }
}