package dev.westelh

import dev.westelh.model.OAuthCodes
import dev.westelh.model.expiresAt
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
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
                if (session != null) {
                    val identity = createIdService(httpClient)
                    val providerName = environment.config.property("vault.oauth.provider").getString()
                    val googleId = identity.getGoogleIdFromOidcProvider(providerName, session.accessToken).onFailure {
                        log.warn("Failed to get Google ID from OIDC provider: ${it.message}")
                    }.getOrNull()

                    call.respondHtml(HttpStatusCode.OK) {
                        head {
                            title = "User Summary"
                        }
                        body {
                            h1 { +"Session" }
                            p { +"Current user session is valid until ${session.expiresAt()}" }
                            p { +"Google ID: $googleId" }
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