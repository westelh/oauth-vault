package dev.westelh

import io.ktor.server.application.Application
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureGoogle() {
    routing {
        authenticate("auth-oauth-google") {
            route("/google") {
                get("/login") {
                    call.respondRedirect("/callback")
                }

                get("/callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2 = call.authentication.principal()!!
                    buildApplicationService().initUser(principal).onSuccess {
                        call.respondRedirect("/")
                    }.onFailure { e ->
                        call.respond(e)
                    }
                }
            }
        }
    }
}