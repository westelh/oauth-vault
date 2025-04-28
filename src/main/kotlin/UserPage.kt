package dev.westelh

import dev.westelh.model.OAuthCodes
import dev.westelh.model.expiresAt
import io.ktor.server.application.Application
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.SessionStorageMemory
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions

// User page module

fun Application.configureUserPage() {
    val env = this.environment
    val service = this.buildApplicationService()

    routing {
        route("/user") {
            install(Sessions) {
                cookie<OAuthCodes>("oauth_principal", SessionStorageMemory()) {
                }
            }

            authenticate("auth-oauth-vault") {
                get("/oidc/login") {
                    call.respondRedirect("/oidc/callback")
                }

                get("/oidc/callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2= call.authentication.principal()!!
                    call.sessions.set("oauth_principal", OAuthCodes(principal))
                    call.respondRedirect("/")
                }
            }

            get("/summary") {
                ensureUserSession { principal ->
                    call.respond("Your session is valid. Expires at ${principal.expiresAt()}.")
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