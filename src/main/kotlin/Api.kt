package dev.westelh

import dev.westelh.service.KvService
import dev.westelh.vault.kv
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun Application.configureApi() {
    val kv = createKvService()
    val google = createGoogleService()

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
                            call.respond(e)
                        }
                    }
                }

                get("/token") {
                    ensureJWT { googleID ->
                        kv.getUserOauthCodes(googleID).onSuccess { codes ->
                            call.respond(Json.encodeToString(codes))
                        }.onFailure { e ->
                            call.respond(e)
                        }
                    }
                }

                post("/token/refresh") {
                    ensureJWT { googleID ->
                        getAndRefreshUserToken(googleID).onSuccess {
                            call.respond(HttpStatusCode.OK, "Token refreshed")
                        }.onFailure { e ->
                            call.respond(e)
                        }
                    }
                }

                post("/token/delete") {
                    ensureJWT { googleID ->
                        kv.deleteUserOauthCodes(googleID).onSuccess {
                            call.respond(HttpStatusCode.OK, "Token deleted")
                        }.onFailure { e ->
                            call.respond(e)
                        }
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
