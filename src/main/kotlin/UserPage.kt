package dev.westelh

import dev.westelh.model.OAuthCodes
import dev.westelh.model.expiresAt
import io.ktor.client.HttpClient
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.html.respondHtml
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.SessionStorageMemory
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.title

// User page module
fun Application.configureUserPage(httpClient: HttpClient = applicationHttpClient) {
    val identity = createIdService(httpClient)

    routing {
        route("/user") {
            install(Sessions) {
                cookie<OAuthCodes>("oauth_principal", SessionStorageMemory()) {
                }
            }

            authenticate("auth-oauth-vault") {
                get("/oidc/login") {
                    // Redirect to the "authorizationUrl"
                }

                get("/oidc/callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2= call.authentication.principal()!!
                    call.sessions.set("oauth_principal", OAuthCodes(principal))
                    call.respondRedirect("/user/summary")
                }
            }

            get("/summary") {
                ensureUserSession { principal ->
                    val providerName = environment.config.property("vault.oauth.provider").getString()
                    val googleId = identity.getGoogleIdFromOidcProvider(providerName, principal.accessToken).getOrNull()

                    call.respondHtml(HttpStatusCode.OK) {
                        head {
                            title = "User Summary"
                        }
                        body {
                            h1 { +"Session" }
                            p { +"Current user session is valid until ${principal.expiresAt()}" }
                            p { +"Google ID: $googleId" }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun RoutingContext.ensureUserSession(block: suspend (OAuthCodes) -> Unit) {
    val principal = call.sessions.get<OAuthCodes>()
    if (principal == null) {
        call.respondRedirect("/user/oidc/login")
    } else {
        block(principal)
    }
}