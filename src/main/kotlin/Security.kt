package dev.westelh

import dev.westelh.vault.Vault
import dev.westelh.vault.IdentityPathBuilder
import dev.westelh.vault.api.identity.GetOidcClientResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.coroutines.runBlocking

fun Application.configureSecurity() {
    val env = this.environment
    val http = HttpClient(Apache) {
        install(ContentNegotiation) {
            json()
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            with(env.config.config("vault.jwt")) {
                verifier(JwkProvider(Vault(VaultApplicationConfig(env.config)))) {
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

        oauth("auth-oauth-vault") {
            val origin = env.config.property("vault.addr").getString()

            with(env.config.config("vault.oauth")) {
                val vault = Vault(VaultApplicationConfig(env.config))

                val clientName = property("client").getString()
                val oidc = runBlocking {
                    vault.get(IdentityPathBuilder(origin).buildOidcClientPath(clientName)) {
                    }.body<GetOidcClientResponse>()
                }

                client = http
                urlProvider = { property("callback").getString() }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "vault",
                        requestMethod = HttpMethod.Post,
                        authorizeUrl = property("authorizeUrl").getString(),
                        accessTokenUrl = property("accessTokenUrl").getString(),
                        clientId = oidc.data.clientId,
                        clientSecret = oidc.data.clientSecret,
                        defaultScopes = property("scopes").getList(),
                        onStateCreated = { call, _->
                            call.request.queryParameters["error"]?.let {
                                val desc = call.request.queryParameters["error_description"].orEmpty()
                                call.application.log.error("Error during oauth: $it - $desc")
                            }
                        }
                    )
                }
            }
        }

        oauth("auth-oauth-google") {
            with(env.config.config("google.oauth")) {
                urlProvider = { property("callback").getString() }
                client = http
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "google",
                        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                        accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                        requestMethod = HttpMethod.Post,
                        clientId = property("clientId").getString(),
                        clientSecret = property("clientSecret").getString(),
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
