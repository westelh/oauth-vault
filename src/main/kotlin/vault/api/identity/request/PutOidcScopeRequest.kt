package dev.westelh.vault.api.identity.request

import kotlinx.serialization.Serializable

@Serializable
data class PutOidcScopeRequest(
    val template: String?,
    val description: String?,
)
