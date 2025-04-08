package dev.westelh

import dev.westelh.vault.Vault
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    authentication {
        jwt("auth-jwt") {
            val jwtConfig = this@configureRouting.environment.config.config("vault.jwt")
            verifier(VaultJwkProvider(Vault(this@configureRouting.vaultConfig()))) {
                withAudience(jwtConfig.property("audience").getString())
                withIssuer(jwtConfig.property("issuer").getString())
                withClaimPresence("google_id")
            }
            validate { credential ->
                JWTPrincipal(credential.payload)
            }
            challenge { _, _ ->
            }
        }
    }

    routing {
        get("/") {
            call.respond("Vault OAuth Client")
        }

        authenticate("auth-jwt") {
            get("/user/id") {
                val id = call.jwt().googleId()
                call.respond(id)
            }

            get("/user/metadata") {
                val id = call.jwt().googleId()
                val client = createVaultClient()

                client.readTokenMetadata(id).onSuccess {
                    call.respond(it)
                }.onFailure {
                    call.respond(HttpStatusCode.NotFound, "Metadata not found")
                }
            }

            get("/token") {
                val id = call.jwt().googleId()
                val client = createVaultClient()

                client.readToken(id).onSuccess { codes ->
                    call.respond(Json.encodeToString(codes))
                }.onFailure {
                    call.respond(HttpStatusCode.NotFound, "Token not found")
                }
            }

            post("/token/refresh") {
                val id = call.jwt().googleId()
                val client = createVaultClient()

                // アカウントIDに対応するトークンを取得
                client.readToken(id).onSuccess { token ->
                    if (token.refreshToken != null) {
                        val new = refresh(token.refreshToken)?.let {
                            // accessToken, expiresIn, createdAtのみ更新
                            token.copy(accessToken = it.accessToken, expiresIn = it.expiresIn, createdAt = Clock.System.now())
                        }

                        if (new != null) {
                            client.writeToken(id, new).onSuccess {
                                call.respond(HttpStatusCode.OK, "Successfully refreshed token")
                            }.onFailure {
                                call.respond(HttpStatusCode.InternalServerError, "Failed to write the backend")
                            }
                        } else { // new == null
                            call.respond(HttpStatusCode.Forbidden, "Refresh token is invalid")
                        }
                    }
                    else {  // token.refreshToken == null
                        call.respond(HttpStatusCode.NotFound, "Refresh token not found")
                    }
                }.onFailure { // client.readToken(id) Failed
                    call.respond(HttpStatusCode.NotFound, "OAuth codes not found")
                }
            }

            post("/token/delete") {
                val id = call.jwt().googleId()
                val client = createVaultClient()

                client.readToken(id).onSuccess { // user has codes on kv
                    client.deleteToken(id).onSuccess {
                        call.respond(HttpStatusCode.OK, "OAuth codes are deleted")
                    }.onFailure {
                        call.respond(HttpStatusCode.InternalServerError, "token is present on the server, but failed to delete it")
                    }
                }.onFailure {
                    call.respond(HttpStatusCode.NotFound, "token not found")
                }
            }
        }
    }
}

fun RoutingCall.jwt(): JWTPrincipal = principal()!!

fun JWTPrincipal.googleId(): String = payload.getClaim("google_id").asString()
