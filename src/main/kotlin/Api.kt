package dev.westelh

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.interfaces.Payload
import dev.westelh.vault.Vault
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

    routing {
        authenticate("auth-jwt") {
            route("/api") {
                get("/user/id") {
                    call.principal<JWTPrincipal>()?.payload?.getGoogleClaim()?.let {
                       call.respond(it)
                    }
                }

                get("/user/metadata") {
                    call.principal<JWTPrincipal>()?.payload?.getGoogleClaim()?.let {
                        val kv = createKvService(httpClient)
                        kv.getUserProfile(it).onSuccess { profile ->
                            call.respond(profile)
                        }.onFailure { error ->
                            call.respond(HttpStatusCode.BadRequest, "Failed to read user metadata")
                            throw error
                        }
                    }
                }

                get("/token") {
                    call.principal<JWTPrincipal>()?.payload?.getGoogleClaim()?.let {
                        val kv = createKvService(httpClient)
                        kv.getUserOauthCodes(it).onSuccess { codes ->
                            call.respond(Json.encodeToString(codes))
                        }.onFailure { error ->
                            call.respond(HttpStatusCode.BadRequest, "Token is missing")
                            throw error
                        }
                    }
                }

                post("/token/refresh") {
                    call.principal<JWTPrincipal>()?.payload?.getGoogleClaim()?.let {
                        val kv = createKvService(httpClient)
                        val google = createGoogleService(httpClient)

                        kv.getUserOauthCodes(it).onSuccess { codes ->
                            val refreshToken = codes.refreshToken
                            if (refreshToken != null) {
                                runCatching {
                                    val new = google.refreshUserToken(refreshToken).getOrThrow()
                                    kv.patchUserOauthCodes(it, new).getOrThrow()
                                    call.respond("Token refreshed")
                                }.onFailure { error ->
                                    call.respond(HttpStatusCode.BadRequest,"Failed to refresh token")
                                    throw error
                                }
                            } else {
                                call.respond(HttpStatusCode.BadRequest, "Refresh token is not present")
                                throw NullPointerException("User refresh token is not present")
                            }
                        }.onFailure { error ->
                            call.respond("Failed to read user token")
                            throw error
                        }
                    }
                }

                post("/token/delete") {
                    call.principal<JWTPrincipal>()?.payload?.getGoogleClaim()?.let {
                        val kv = createKvService(httpClient)
                        kv.deleteUserOauthCodes(it).getOrThrow()
                        call.respond("Token deleted (if it existed)")
                    }
                }
            }
        }
    }
}

private fun Payload.getGoogleClaim() = getClaim("google_id").asString()

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
