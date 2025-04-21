package dev.westelh.vault.api.identity.response

import dev.westelh.vault.api.GetSecretResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias GetOidcClientResponse = GetSecretResponse<GetOidcClientResponseData>

@Serializable
data class GetOidcClientResponseData(
    @SerialName("access_token_ttl")
    val accessTokenTtl: Long,

    val assignments: List<String>,

    @SerialName("client_id")
    val clientId: String,

    @SerialName("client_secret")
    val clientSecret: String,

    @SerialName("client_type")
    val clientType: String,

    @SerialName("id_token_ttl")
    val idTokenTtl: Long,

    val key: String,

    @SerialName("redirect_uris")
    val redirectUris: List<String>
)