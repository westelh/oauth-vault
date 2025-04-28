package dev.westelh.vault.api.kv.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class MetadataSnapshot(
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
