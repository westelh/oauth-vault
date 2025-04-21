package dev.westelh.vault.api.identity

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

// https://developer.hashicorp.com/vault/api-docs/secret/identity/tokens#read-identity-token-issuer-s-public-jwks

@Serializable
data class GetIdentityTokenIssuerKeysResponse(
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