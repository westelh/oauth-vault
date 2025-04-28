package dev.westelh.vault.api.kv.v2.response

import dev.westelh.vault.api.kv.v2.MetadataSnapshot
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GetSecretSubkeysResponse(
    val subkeys: JsonObject,
    val metadata: MetadataSnapshot
)
