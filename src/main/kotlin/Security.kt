package dev.westelh

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity(client: HttpClient = applicationHttpClient) {
    val env = this.environment

    val id = createIdService(client)
    val provider = createJwkProvider(client)

    install(Authentication) {
        jwt("auth-jwt") {
            with(env.config.config("vault.jwt")) {
                val audience = property("audience").getString()
                val issuer = property("issuer").getString()

                verifier(provider) {
                    withAudience(audience)
                    withIssuer(issuer)
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

        oauth("auth-oauth-vault") {
            with(env.config.config("vault.oauth")) {
                val providerName = property("provider").getString()
                val clientName = property("client").getString()
                val callback = property("callback").getString()
                val scopes = property("scopes").getList()

                this@oauth.client = client
                urlProvider = { callback }
                providerLookup = { id.buildProviderLookup(providerName, clientName, scopes) }
            }
        }

        oauth("auth-oauth-google") {
            with(env.config.config("google.oauth")) {
                val callback = property("callback").getString()
                val clientId = property("clientId").getString()
                val clientSecret = property("clientSecret").getString()

                urlProvider = { callback }
                this@oauth.client = client
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "google",
                        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                        accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                        requestMethod = HttpMethod.Post,
                        clientId = clientId,
                        clientSecret = clientSecret,
                        defaultScopes = listOf(
                            "https://www.googleapis.com/auth/userinfo.profile",
                            "https://www.googleapis.com/auth/userinfo.email",
                            "https://mail.google.com/",
                        ),
                        extraAuthParameters = listOf("access_type" to "offline")
                    )
                }
            }
        }
    }
}

