package dev.westelh

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.SigningKeyNotFoundException
import dev.westelh.vault.api.identity.Identity
import dev.westelh.vault.identity
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

class JwkProvider(val identity: Identity): JwkProvider {
    companion object {
        fun decodeJwk(json: JsonElement): Jwk = Jwk.fromValues(Json.decodeFromJsonElement<Map<String, String>>(json))
    }

    override fun get(keyId: String): Jwk {
        return runBlocking {
            identity.getIdentityTokenIssuerKeys().mapCatching {
                it.keys.find { it.keyId == keyId }.let { found ->
                    decodeJwk(Json.encodeToJsonElement(found))
                }
            }.getOrElse {
                throw SigningKeyNotFoundException("Signing key for id $keyId not found", it)
            }
        }
    }
}
