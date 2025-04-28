package dev.westelh.vault.api.kv.v2.response

import dev.westelh.vault.api.GetSecretResponse
import dev.westelh.vault.api.kv.v2.MetadataSnapshot
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

typealias GetSecretVersionResponse = GetSecretResponse<GetSecretVersionResponseData>

@Serializable
data class GetSecretVersionResponseData(
    val data: JsonObject,
    val metadata: MetadataSnapshot,
)