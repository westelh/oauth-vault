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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.h1
import kotlinx.html.onClick
import kotlinx.html.p
import kotlinx.html.title

// User page module
fun Application.configureUserPage(httpClient: HttpClient = applicationHttpClient) {
    install(Sessions) {
        cookie<OAuthCodes>("oauth_principal", SessionStorageMemory())
    }

    routing {
        route("/user") {
            get("/summary") {
                val session = getUserSession(call)

                call.respondHtml(HttpStatusCode.OK) {
                    head {
                        title = "User Summary"
                    }
                    body {
                        if (session != null) {
                            runBlocking {
                                h1 { +"You are authorized via Vault OIDC" }
                                p { +"Current user session is valid until ${session.expiresAt()}" }

                                val identity = createIdService(httpClient)
                                val providerName = environment.config.property("vault.oauth.provider").getString()
                                val google =
                                    identity.getGoogleIdFromOidcProvider(providerName, session.accessToken).onFailure {
                                        log.warn("Failed to get Google ID from OIDC provider: ${it.message}")
                                    }.map {
                                        it.id
                                    }.getOrNull()
                                p { +"Google ID: $google" }

                                if (google != null) {
                                    val kv = createKvService(httpClient)
                                    kv.getUserOauthCodes(google).onSuccess {
                                        h1 { +"User OAuth Codes" }
                                        p { +"Access Token: ${it.accessToken}" }
                                        p { +"Refresh Token: ${it.refreshToken}" }
                                        p { +"Created at ${it.createdAt.toLocalDateTime(TimeZone.of("Asia/Tokyo"))}" }
                                        p { +"Expires at ${it.expiresAt().toLocalDateTime(TimeZone.of("Asia/Tokyo"))}" }
                                    }.onFailure {
                                        log.warn("Failed to get user OAuth codes: ${it.message}")
                                    }
                                }
                            }
                        } else {
                            h1 { +"No session" }
                            p { +"No user session found. Please log in." }
                            p {
                                a(href = "/user/oidc/login") { +"Log in" }
                            }
                        }
                        h1 {
                            button {
                                onClick = "location.href='/google/login'"
                                +"Google Login"
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getUserSession(call: ApplicationCall): OAuthCodes? {
    return call.sessions.get<OAuthCodes>()
}
