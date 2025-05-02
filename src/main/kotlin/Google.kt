package dev.westelh

import dev.westelh.model.OAuthCodes
import dev.westelh.service.GoogleService
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class ApplicationGoogleConfig(applicationConfig: ApplicationConfig) : GoogleService.Config {
    val config = applicationConfig.config("oauth.google")
    override val clientId: String = config.property("clientId").getString()
    override val clientSecret: String = config.property("clientSecret").getString()
}

fun Application.configureGoogleOAuth(httpClient: HttpClient) {
    val env = this.environment
    val kv = createKvService(httpClient)
    val google = GoogleService(httpClient, ApplicationGoogleConfig(environment.config))

    tryInstallAuthentication()
    plugin(Authentication).configure {
        oauth("auth-oauth-google") {
            val callback = env.config.config("oauth.google.callback").toString()
            urlProvider = { callback }
            client = httpClient
            providerLookup = {
                google.oauth2Settings(
                    listOf(
                        "https://www.googleapis.com/auth/userinfo.profile",
                        "https://www.googleapis.com/auth/userinfo.email",
                        "https://mail.google.com/",
                    )
                )
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
                        log.warn("Failed to initialize user: ${e.message}")
                        call.respond(HttpStatusCode.InternalServerError, "Failed to initialize user")
                    }
                }
            }
        }
    }
}
