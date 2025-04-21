package dev.westelh.vault.api.kv.v2.request

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PutSecretRequest(
    val data: JsonElement,
    val options: Options? = null
) {
    @Serializable
    data class Options(val cas: Int?)
}
