package dev.westelh.vault.api.kv.v2

import kotlinx.serialization.Serializable

@Serializable
data class SecretVersionSpec(
    val versions: List<Int>
)
