package dev.westelh.vault.api.identity.response

import dev.westelh.vault.api.GetSecretResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias GetOidcAssignmentResponse = GetSecretResponse<GetOidcAssignmentResponseData>
typealias ListOidcAssignmentsResponse = GetSecretResponse<ListResponseData>

@Serializable
data class GetOidcAssignmentResponseData(
    @SerialName("entity_ids")
    val entityIds: List<String>,

    @SerialName("group_ids")
    val groupIds: List<String>,
)

