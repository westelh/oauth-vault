package dev.westelh.vault.api.kv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class KvV2WriteSecretRequest(
    val data: JsonElement,
    val options: Options? = null
) {
    @Serializable
    data class Options(val cas: Int?)
}

@Serializable
data class KvV2WriteSecretResponse(
    @SerialName("request_id")
    val requestId: String,
    @SerialName("lease_id")
    val leaseId: String,
    @SerialName("renewable")
    val renewable: Boolean,
    @SerialName("lease_duration")
    val leaseDuration: Int,
    @SerialName("data")
    val data: Data,
    @SerialName("wrap_info")
    val wrapInto: String? = null,
    @SerialName("warnings")
    val warnings: String? = null,
    @SerialName("auth")
    val auth: String? = null,
    @SerialName("mount_type")
    val mountType: String
) {
    @Serializable
    data class Data(
        @SerialName("created_time")
        val createdTime: String,
        @SerialName("custom_metadata")
        val customMetadata: JsonObject,
        @SerialName("deletion_time")
        val deletionTime: String,
        @SerialName("destroyed")
        val destroyed: Boolean,
        @SerialName("version")
        val version: Int
    )
}
