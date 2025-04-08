package dev.westelh.vault.api.kv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// KvV2GetMetadata does not have a request body

@Serializable
data class KvV2GetMetadataResponse(
    @SerialName("request_id")
    val requestId: String,
    @SerialName("lease_id")
    val leaseId: String,
    @SerialName("renewable")
    val renewable: Boolean,
    @SerialName("lease_duration")
    val leaseDuration: Long,
    @SerialName("warnings")
    val warnings: String?,
    @SerialName("data")
    val data: Data,
) {
    @Serializable
    data class Data(
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
}