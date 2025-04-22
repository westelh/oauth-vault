package dev.westelh.vault.api.identity.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PutOidcAssignmentRequest(
    @SerialName("entity_ids")
    val entityIds: List<String> = emptyList(),

    @SerialName("group_ids")
    val groupIds: List<String> = emptyList(),
)
