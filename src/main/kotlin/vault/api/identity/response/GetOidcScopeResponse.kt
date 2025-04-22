package dev.westelh.vault.api.identity.response

import dev.westelh.vault.api.GetSecretResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias GetOidcScopeResponse = GetSecretResponse<GetOidcScopeResponseData>
typealias ListOidcScopesResponse = GetSecretResponse<ListResponseData>

@Serializable
data class GetOidcScopeResponseData(
    @SerialName("description")
    val description: String?,

    @SerialName("template")
    val template: String?,
)
