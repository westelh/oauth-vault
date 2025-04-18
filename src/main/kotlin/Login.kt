package dev.westelh

import dev.westelh.vault.Vault
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    val env = this.environment
    install(Authentication) {
        jwt("auth-jwt") {
            with(env.config.config("vault.jwt")) {
                verifier(VaultJwkProvider(Vault(env.vaultConfig()))) {
                    withAudience(property("audience").getString())
                    withIssuer(property("issuer").getString())
                    withClaimPresence("google_id")
                }
                validate { credential ->
                    JWTPrincipal(credential.payload)
                }
                challenge { _, _ ->
                    call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
                }
            }
        }

        val google = OAuthServerSettings.OAuth2ServerSettings(
            name = "google",
            authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
            accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
            requestMethod = HttpMethod.Post,
            clientId = this@configureSecurity.environment.config.property("google.oauth.clientId").getString(),
            clientSecret = this@configureSecurity.environment.config.property("google.oauth.clientSecret").getString(),
            defaultScopes = listOf(
                "https://www.googleapis.com/auth/userinfo.profile",
                "https://www.googleapis.com/auth/userinfo.email",
                "https://mail.google.com/",
            ),
            extraAuthParameters = listOf("access_type" to "offline")
        )
        oauth("auth-oauth-google") {
            urlProvider = { this@configureSecurity.environment.config.property("google.oauth.callback").getString() }
            providerLookup = { google }
            client = HttpClient(Apache) {
                install(Logging) {
                    level = LogLevel.INFO
                }
            }
        }
    }
}

