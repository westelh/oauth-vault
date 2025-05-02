package dev.westelh

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.SigningKeyNotFoundException
import dev.westelh.vault.api.identity.Identity
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

fun Application.configureApi(httpClient: HttpClient) {
    val apiConfig = environment.config.config("api")

    tryInstallAuthentication()
    plugin(Authentication).configure {
        val service = this@configureApi.createIdService(httpClient)

        jwt("auth-jwt") {
            val jwtConfig = apiConfig.config("auth.jwt")

            verifier(VaultIdentityTokenKeyProvider(service.identity)) {
                jwtConfig.propertyOrNull("audience")?.getString()?.let { aud ->
                    withAudience(aud)
                }
                jwtConfig.propertyOrNull("issuer")?.getString()?.let { iss ->
                    withIssuer(iss)
                }
            }
            validate { credential ->
                JWTPrincipal(credential.payload)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    suspend fun getAndRefreshUserToken(userId: String): Result<Unit> {
        val kv = createKvService(httpClient)
        val google = createGoogleService(httpClient)
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
                        val kv = createKvService(httpClient)
                        kv.getUserProfile(googleID).onSuccess {
                            call.respond(it)
                        }.onFailure { e ->
                            log.warn(makeLogMessage(this.call.request, e))
                        }
                    }
                }

                get("/token") {
                    ensureJWT { googleID ->
                        val kv = createKvService(httpClient)
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
                        val kv = createKvService(httpClient)
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

class VaultIdentityTokenKeyProvider(val identity: Identity) : JwkProvider {
    companion object {
        fun decodeJwk(json: JsonElement): Jwk = Jwk.fromValues(Json.decodeFromJsonElement<Map<String, String>>(json))
    }

    override fun get(keyId: String): Jwk = runBlocking {
        identity.getIdentityTokenIssuerKeys().mapCatching {
            it.keys.find { it.keyId == keyId }.let { found ->
                decodeJwk(Json.encodeToJsonElement(found))
            }
        }.getOrElse {
            throw SigningKeyNotFoundException("Signing key for id $keyId not found", it)
        }
    }
}
