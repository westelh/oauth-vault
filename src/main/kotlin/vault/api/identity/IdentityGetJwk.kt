package dev.westelh.vault.api.identity

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys


// Path: /identity/oidc/.well-known/keys

// IdentityGetJwk does not have a request body

@Serializable
data class IdentityGetJwkResponseData(
    val keys: List<JwkKey>,
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class JwkKey(
        val alg: String,
        val e: String,
        @SerialName("kid") val keyId: String,
        val kty: String,
        val n: String,
        val use: String
    )
}