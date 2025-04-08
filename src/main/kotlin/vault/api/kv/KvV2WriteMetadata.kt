package dev.westelh.vault.api.kv

import dev.westelh.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#create-update-metadata
@Serializable
data class KvV2WriteMetadataRequest (
    @SerialName("custom_metadata")
    val customMetadata: UserProfile,
    @SerialName("max-versions")
    val maxVersions: Int = 0,
    @SerialName("cas_required")
    val casRequired: Boolean = false,
    @SerialName("delete_version_after")
    val deleteVersionAfter: String = "0s",
)

// KvV2WriteMetadata does not have a response body. API returns 204 No Content.