package dev.westelh

import dev.westelh.model.OAuthCodes
import dev.westelh.service.ApplicationGoogleService
import dev.westelh.service.KvService
import dev.westelh.vault.api.kv.v2.Kv
import io.ktor.client.engine.apache.Apache
import io.ktor.server.application.Application
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureGoogle() {
    val google = createGoogleService()
    val kv = createKvService()

    suspend fun initUser(oauth2: OAuthAccessTokenResponse.OAuth2) = runCatching {
        val user = google.getUser(oauth2.accessToken).getOrThrow()
        kv.writeUserProfile(user).getOrThrow()
        kv.writeUserOauthCodes(user.id, OAuthCodes(oauth2)).getOrThrow()
    }

    routing {
        authenticate("auth-oauth-google") {
            route("/google") {
                get("/login") {
                    call.respondRedirect("/callback")
                }

                get("/callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2 = call.authentication.principal()!!
                    initUser(principal).onSuccess {
                        call.respondRedirect("/")
                    }.onFailure { e ->
                        call.respond(e)
                    }
                }
            }
        }
    }


}