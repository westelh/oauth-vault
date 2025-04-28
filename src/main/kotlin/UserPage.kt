package dev.westelh

import dev.westelh.model.OAuthCodes
import dev.westelh.model.expiresAt
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.p
import kotlinx.html.title

// User page module
fun Application.configureUserPage(httpClient: HttpClient = applicationHttpClient) {
    val identity = createIdService(httpClient)
    val env = this.environment

    plugin(Authentication).configure {
        oauth("auth-oauth-vault") {
            with(env.config.config("vault.oauth")) {
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