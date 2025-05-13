package dev.westelh

import dev.westelh.model.OAuthCodes
import dev.westelh.model.expiresAt
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.p
import kotlinx.html.title

// User page module
fun Application.configureUserPage(httpClient: HttpClient) {
    install(Sessions) {
        cookie<OAuthCodes>("oauth_principal", SessionStorageMemory())
    }

    routing {
        route("/user") {
            get("/summary") {
                val session = call.sessions.get<OAuthCodes>()
                val identity = createIdService(httpClient)
                val kv = createKvService(httpClient)
                val providerName = environment.config.property("user.oidc.provider").getString()
                val tz = TimeZone.of("Asia/Tokyo")

                call.respondHtml(HttpStatusCode.OK) {
                    head {
                        title = "User Summary"
                    }
                    body {
                        if (session != null) {
                            runBlocking {
                                identity.getGoogleIdFromOidcProvider(providerName, session.accessToken).onSuccess {
                                    h1 { +"You are authorized via Vault OIDC" }
                                    p { +"Current user session is valid until ${session.expiresAt().toLocalDateTime(tz)}" }

                                    kv.getUserOauthCodes(it.id).onSuccess {
                                        h2 { +"User OAuth Codes" }
                                        p { +"Access Token: ${if (it.accessToken.isNotBlank()) "✅ Present" else "❓ Missing"}" }
                                        p { +"Refresh Token: ${if (!it.refreshToken.isNullOrBlank()) "✅ Present" else "❓ Missing"}" }
                                        p { +"Created at ${it.createdAt.toLocalDateTime(tz)}" }
                                        p { +"Expires at ${it.expiresAt().toLocalDateTime(tz)}" }
                                        if (it.expiresAt() < Clock.System.now()) {
                                            a(href = "/google/login") { +"Login with Google" }
                                        }
                                    }.onFailure {
                                        h2 { +"OAuth codes not found" }
                                    }
                                }.onFailure { e ->
                                    h1 { +"You are not registered user" }
                                    log.info(e.message)
                                }
                            }
                        }
                        else {
                            h1 { +"No session" }
                            p { +"No user session found. Please log in." }
                            p {
                                a(href = "/user/oidc/login") { +"Log in" }
                            }
                        }
                    }
                }
            }
        }
    }
}
