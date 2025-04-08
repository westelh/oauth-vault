package dev.westelh

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSecurity() {
    val google = OAuthServerSettings.OAuth2ServerSettings(
        name = "google",
        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
        accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
        requestMethod = HttpMethod.Post,
        clientId = environment.config.property("google.oauth.clientId").getString(),
        clientSecret = environment.config.property("google.oauth.clientSecret").getString(),
        defaultScopes = listOf(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://mail.google.com/",
        ),
        extraAuthParameters = listOf("access_type" to "offline")
    )

    val environment = environment

    authentication {
        oauth("auth-oauth-google") {
            urlProvider = { environment.config.property("google.oauth.callback").getString() }
            providerLookup = { google }
            client = HttpClient(Apache) {
                install(Logging) {
                    level = LogLevel.INFO
                }
            }
        }
    }

    routing {
        authenticate("auth-oauth-google") {
            get("/login") {
                call.respondRedirect("/callback")
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2 = call.authentication.principal()!!

                // First login
                if (principal.refreshToken != null) {
                    val user = getUser(principal.accessToken)!!
                    val vault = createVaultClient()
                    vault.writeToken(user.id, OAuthCodes(principal))
                    vault.writeTokenMetadata(user.id, user)
                }

                call.respondRedirect("/")
            }
        }
    }
}

