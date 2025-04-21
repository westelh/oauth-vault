package dev.westelh.vault.api.kv.v2.response

import dev.westelh.vault.api.GetSecretResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

typealias GetSecretMetadataResponse = GetSecretResponse<GetSecretMetadataResponseData>

@Serializable
data class GetSecretMetadataResponseData(
    @SerialName("cas_required")
    val casRequired: Boolean,
    @SerialName("created_time")
    val createdTime: String,
    @SerialName("current_version")
    val currentVersion: Int,
    @SerialName("delete_version_after")
    val deleteVersionAfter: String,
    @SerialName("max_versions")
    val maxVersions: Int,
    @SerialName("oldest_version")
    val oldestVersion: Int,
    @SerialName("updated_time")
    val updatedTime: String,
    @SerialName("custom_metadata")
    val customMetadata: JsonObject,
    @SerialName("versions")
    val versions: Map<Int, Version>
) {
    @Serializable
    data class Version(
        @SerialName("created_time")
        val createdTime: String,
        @SerialName("deletion_time")
        val deletionTime: String,
        @SerialName("destroyed")
        val destroyed: Boolean,
    )
}