package dev.westelh.vault.api.kv

import kotlinx.serialization.Serializable

@Serializable
data class KvV2DeleteSecretVersionRequest(
    val versions: List<Int>
)

// Response is not defined. API returns 204 No Content