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
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.h1
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
                                    kv.getUserProfile(google).onSuccess {
                                        val userProfile = it
                                        h1 { +"User Profile" }
                                        p { +"Name: ${userProfile.name}" }
                                        p { +"Email: ${userProfile.email}" }
                                    }.onFailure {
                                        log.warn("Failed to get user profile: ${it.message}")
                                        h1 { +"Error" }
                                        p { +"Failed to get user profile." }
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
                    }
                }
            }
        }
    }
}

private fun getUserSession(call: ApplicationCall): OAuthCodes? {
    return call.sessions.get<OAuthCodes>()
}
