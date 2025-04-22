package dev.westelh.vault.api.identity.response

import dev.westelh.vault.api.GetSecretResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias GetOidcProviderResponse = GetSecretResponse<GetOidcProviderResponseData>
typealias ListOidcProvidersResponse = GetSecretResponse<ListResponseDataWithKeyInfo<GetOidcProviderResponseData>>

@Serializable
data class GetOidcProviderResponseData(
    val issuer: String?,

    @SerialName("allowed_client_ids")
    val allowedClientIds: List<String>?,

    @SerialName("scopes_supported")
    val scopesSupported: List<String>?,
)