package dev.westelh.vault.api.kv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// KvV2Get does not have a request body

@Serializable
data class KvV2GetSecretResponse(
    @SerialName("request_id") val requestId: String,
    @SerialName("lease_id") val leaseId: String,
    val renewable: Boolean,
    @SerialName("lease_duration") val leaseDuration: Int,
    val data: DataContainer,
    @SerialName("wrap_info") val wrapInfo: String? = null,
    val warnings: String? = null,
    val auth: String? = null,
    @SerialName("mount_type") val mountType: String
) {
    @Serializable
    data class DataContainer(
        val data: JsonObject,
        val metadata: JsonObject
    )
}