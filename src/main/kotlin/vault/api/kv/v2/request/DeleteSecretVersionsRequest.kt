package dev.westelh.vault.api.kv.v2.request

import kotlinx.serialization.Serializable

@Serializable
data class DeleteSecretVersionsRequest(
    val versions: List<Int>
)
