package dev.westelh.vault.api.kv.v2.request

import kotlinx.serialization.Serializable

@Serializable
data class PutKvConfigRequest(
    val maxVersions: Int = 0,
    val casRequired: Boolean = false,
    val deleteVersionAfter: String = "0s",
)
