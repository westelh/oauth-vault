package dev.westelh

import dev.westelh.model.OAuthCodes
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureGoogle(httpClient: HttpClient = applicationHttpClient) {
    val env = this.environment
    val google = createGoogleService(httpClient)
    val kv = createKvService(httpClient)

    plugin(Authentication).configure {
        oauth("auth-oauth-google") {
            with(env.config.config("google.oauth")) {
                val callback = property("callback").getString()
                val clientId = property("clientId").getString()
                val clientSecret = property("clientSecret").getString()

                urlProvider = { callback }
                client = httpClient
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
                        call.respondError(e)
                    }
                }
            }
        }
    }


}