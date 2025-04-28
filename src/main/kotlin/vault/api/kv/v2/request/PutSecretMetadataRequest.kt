package dev.westelh.vault.api.kv.v2.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

typealias PatchSecretMetadataRequest = PutSecretMetadataRequest

@Serializable
data class PutSecretMetadataRequest (
    @SerialName("custom_metadata")
    val customMetadata: JsonElement,
    @SerialName("max-versions")
    val maxVersions: Int = 0,
    @SerialName("cas_required")
    val casRequired: Boolean = false,
    @SerialName("delete_version_after")
    val deleteVersionAfter: String = "0s",
)
