package dev.westelh.vault.api.kv.v2.request

import kotlinx.serialization.Serializable

@Serializable
data class DeleteSecretVersionRequest(
    val versions: List<Int>
)
