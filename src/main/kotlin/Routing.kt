package dev.westelh

import dev.westelh.vault.Vault
import dev.westelh.vault.VaultConfig
import dev.westelh.vault.api.kv.KvV2GetSecretResponse
import dev.westelh.vault.api.kv.KvV2WriteSecretResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond("Vault OAuth Client")
        }

        authenticate("auth-oauth-google") {
            get("/login") {
                call.respondRedirect("/callback")
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2 = call.authentication.principal()!!

                // First login
                if (principal.refreshToken != null) {
                    val user = getUser(principal.accessToken)!!
                    val vault = createVaultClient()
                    vault.writeToken(user.id, OAuthCodes(principal))
                    vault.writeTokenMetadata(user.id, user)
                }

                call.respondRedirect("/")
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
                    val client = createVaultClient()

                    client.readTokenMetadata(googleID).onSuccess {
                        call.respond(it)
                    }.onFailure {
                        call.respond(HttpStatusCode.NotFound, "Metadata not found")
                    }
                }
            }

            get("/token") {
                ensureJWT { googleID ->
                    val client = createVaultClient()
                    client.readToken(googleID).handleFailureOr(call) { codes ->
                        call.respond(Json.encodeToString(codes))
                    }
                }
            }

            post("/token/refresh") {
                ensureJWT { googleID ->
                    val client = createVaultClient()

                    // アカウントIDに対応するトークンを取得
                    client.readToken(googleID).handleFailureOr(call) { token ->
                        if (token.refreshToken != null) {
                            val new = refresh(token.refreshToken)?.let {
                                // accessToken, expiresIn, createdAtのみ更新
                                token.copy(
                                    accessToken = it.accessToken,
                                    expiresIn = it.expiresIn,
                                    createdAt = Clock.System.now()
                                )
                            }

                            if (new != null) {
                                client.writeToken(googleID, new).handleFailureOr(call) {
                                    call.respond(HttpStatusCode.OK, "Successfully refreshed token")
                                }
                            } else { // new == null
                                call.respond(HttpStatusCode.Forbidden, "Refresh token is invalid")
                            }
                        } else {  // token.refreshToken == null
                            call.respond(HttpStatusCode.NotFound, "Refresh token not found")
                        }
                    }
                }
            }

            post("/token/delete") {
                ensureJWT { googleID ->
                    val client = createVaultClient()

                    client.readToken(googleID).handleFailureOr(call) { // user has codes on kv
                        client.deleteToken(googleID).onSuccess {
                            call.respond(HttpStatusCode.OK, "OAuth codes are deleted")
                        }.onFailure {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                "token is present on the server, but failed to delete it"
                            )
                        }
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

fun Application.createVaultClient(): Client {
    val client = Vault(environment.vaultConfig())
    val mount = environment.config.property("vault.kv").getString()
    return Client(client, mount)
}

fun ApplicationEnvironment.vaultConfig(): VaultConfig = object : VaultConfig {
    override val address: String
        get() = config.property("vault.addr").getString()

    override val token: String
        get() = config.propertyOrNull("vault.token")?.getString() ?: ""
}

suspend fun Result<OAuthCodes>.handleFailureOr(call: ApplicationCall, block: suspend (codes: OAuthCodes) -> Unit) {
    onSuccess { codes ->
        block.invoke(codes)
    }
    onFailure {
        call.respond(HttpStatusCode.InternalServerError, "Failed to read the token")
    }
}

suspend fun Result<KvV2WriteSecretResponse>.handleFailureOr(call: ApplicationCall, block: suspend () -> Unit) {
    onSuccess {
        block.invoke()
    }
    onFailure {
        call.respond(HttpStatusCode.InternalServerError, "Failed to write the token")
    }
}