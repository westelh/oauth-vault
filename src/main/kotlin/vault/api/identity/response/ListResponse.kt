package dev.westelh.vault.api.identity.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListResponseData(
    val keys: List<String>
)

@Serializable
data class ListResponseDataWithKeyInfo<T>(
    val keys: List<String>,

    @SerialName("key_info")
    val keyInfo: Map<String, T>
)
