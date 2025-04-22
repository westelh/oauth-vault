package dev.westelh.vault.api.identity.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PutOidcProviderRequest(
    val issuer: String?,

    @SerialName("allowed_client_ids")
    val allowedClientIds: List<String>?,

    @SerialName("scopes_supported")
    val scopesSupported: List<String>?,
)
