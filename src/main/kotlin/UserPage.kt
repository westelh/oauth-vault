package dev.westelh

import io.ktor.server.application.Application
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions

// User page module

fun Application.configureUserPage() {
    val env = this.environment
    val service = this.buildApplicationService()

    routing {
        route("/user") {
            install(Sessions)

            authenticate("auth-oauth-vault") {
                get("/oidc/login") {
                    call.respondRedirect("/oidc/user/callback")
                }

                get("/oidc/callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2 = call.authentication.principal()!!
                    call.respondRedirect("/")
                }
            }
        }
    }
}