package dev.westelh.vault.api.identity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PutOidcClientRequest(
    @SerialName("access_token_ttl")
    val accessTokenTtl: String = "24h",

    val assignments: List<String> = emptyList(),

    @SerialName("client_type")
    val clientType: String = "confidential",

    @SerialName("id_token_ttl")
    val idTokenTtl: String = "24h",

    val key: String = "default",

    @SerialName("redirect_uris")
    val redirectUris: List<String> = emptyList(),
)