package dev.westelh

import dev.westelh.model.OAuthCodes
import io.ktor.client.HttpClient
import io.ktor.server.application.Application
import io.ktor.server.application.plugin
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.oauth
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.sessions

fun Application.configureVaultOidc(httpClient: HttpClient = applicationHttpClient) {

    plugin(Authentication).configure {
        oauth("auth-oauth-vault") {
            val identity = this@configureVaultOidc.createIdService(httpClient)

            with(this@configureVaultOidc.environment.config.config("vault.oauth")) {
                val providerName = property("provider").getString()
                val clientName = property("client").getString()
                val callback = property("callback").getString()
                val scopes = property("scopes").getList()

                client = httpClient
                urlProvider = { callback }
                providerLookup = { identity.buildProviderLookup(providerName, clientName, scopes) }
            }
        }
    }

    routing {
        route("/user") {
            authenticate("auth-oauth-vault") {
                get("/oidc/login") {
                    // Redirect to the "authorizationUrl"
                }

                get("/oidc/callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2 = call.authentication.principal()!!
                    call.sessions.set("oauth_principal", OAuthCodes(principal))
                    call.respondRedirect("/user/summary")
                }
            }
        }
    }
}