package dev.westelh

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureApi(httpClient: HttpClient = applicationHttpClient) {
    val env = this.environment
    val kv = createKvService(httpClient)
    val google = createGoogleService(httpClient)

    plugin(Authentication).configure {
        jwt("auth-jwt") {
            with(env.config.config("vault.jwt")) {
                val audience = property("audience").getString()
                val issuer = property("issuer").getString()
                val provider = this@configureApi.createJwkProvider(httpClient)

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
    }

    suspend fun getAndRefreshUserToken(userId: String): Result<Unit> {
        return kv.getUserOauthCodes(userId).mapCatching {
            val tok = it.refreshToken!!
            val new = google.refreshUserToken(tok).getOrThrow()
            kv.patchUserOauthCodes(userId, new).getOrThrow()
        }
    }

    routing {
        authenticate("auth-jwt") {
            route("/api") {
                get("/user/id") {
                    ensureJWT { googleID ->
                        call.respond(googleID)
                    }
                }

                get("/user/metadata") {
                    ensureJWT { googleID ->
                        kv.getUserProfile(googleID).onSuccess {
                            call.respond(it)
                        }.onFailure { e ->
                            log.warn(makeLogMessage(this.call.request, e))
                        }
                    }
                }

                get("/token") {
                    ensureJWT { googleID ->
                        kv.getUserOauthCodes(googleID).onSuccess { codes ->
                            call.respond(Json.encodeToString(codes))
                        }.onFailure { e ->
                            log.warn(makeLogMessage(this.call.request, e))
                        }
                    }
                }

                post("/token/refresh") {
                    ensureJWT { googleID ->
                        getAndRefreshUserToken(googleID).onSuccess {
                            call.respond(HttpStatusCode.OK, "Token refreshed")
                        }.onFailure { e ->
                            log.warn(makeLogMessage(this.call.request, e))
                        }
                    }
                }

                post("/token/delete") {
                    ensureJWT { googleID ->
                        kv.deleteUserOauthCodes(googleID).onFailure { e ->
                            log.warn(makeLogMessage(this.call.request, e))
                        }
                        call.respond("Token deleted (if it existed)")
                    }
                }
            }
        }
    }
}

private suspend fun RoutingContext.ensureJWT(block: suspend RoutingContext.(googleID: String) -> Unit) {
    with(call) {
        val principal = principal<JWTPrincipal>()
        if (principal == null) {
            respond(HttpStatusCode.Unauthorized, "JWT token is required")
        } else {
            val googleID = principal.payload.getClaim("google_id")
            if (googleID == null) {
                respond(HttpStatusCode.Unauthorized, "google_id is required")
            } else {
                block(googleID.asString())
            }
        }
    }
}
