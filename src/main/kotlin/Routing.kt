package dev.westelh

import dev.westelh.vault.Vault
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond("Vault OAuth Client")
        }

        authenticate("auth-oauth-vault") {
            get("/oidc/user/login") {
                call.respondRedirect("/oidc/user/callback")
            }

            get("/oidc/user/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2 = call.authentication.principal()!!
                call.respondRedirect("/")
            }
        }

        authenticate("auth-oauth-google") {
            get("/login") {
                call.respondRedirect("/callback")
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2 = call.authentication.principal()!!
                buildApplicationService().initUser(principal).onSuccess {
                    call.respondRedirect("/")
                }.onFailure { e ->
                    call.respond(e)
                }
            }
        }

        authenticate("auth-jwt") {
            get("/user/id") {
                ensureJWT { googleID ->
                    call.respond(googleID)
                }
            }

            get("/user/metadata") {
                ensureJWT { googleID ->
                    buildApplicationService().kv.getUserProfile(googleID).onSuccess {
                        call.respond(it)
                    }.onFailure { e ->
                        call.respond(e)
                    }
                }
            }

            get("/token") {
                ensureJWT { googleID ->
                    buildApplicationService().kv.getUserOauthCodes(googleID).onSuccess { codes ->
                        call.respond(Json.encodeToString(codes))
                    }.onFailure { e ->
                        call.respond(e)
                    }
                }
            }

            post("/token/refresh") {
                ensureJWT { googleID ->
                    buildApplicationService().getAndRefreshUserToken(googleID).onSuccess {
                        call.respond(HttpStatusCode.OK, "Token refreshed")
                    }.onFailure { e ->
                        call.respond(e)
                    }
                }
            }

            post("/token/delete") {
                ensureJWT { googleID ->
                    buildApplicationService().kv.deleteUserOauthCodes(googleID).onSuccess {
                        call.respond(HttpStatusCode.OK, "Token deleted")
                    }.onFailure { e ->
                        call.respond(e)
                    }
                }
            }
        }
    }
}

suspend fun RoutingContext.ensureJWT(block: suspend RoutingContext.(googleID: String) -> Unit) {
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

suspend fun RoutingCall.respond(e: Throwable) {
    val path = this.request.path()

    when (e) {
        is Vault.VaultError -> {
            this.respond(e.response.status, "$path ${e.message}")
        }
        else -> {
            this.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
        }
    }
}