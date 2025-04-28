package dev.westelh.vault.api.kv.v2.response

import dev.westelh.vault.api.GetSecretResponse
import kotlinx.serialization.Serializable

typealias GetKvConfigResponse = GetSecretResponse<GetKvConfigResponseData>

@Serializable
data class GetKvConfigResponseData(
    val maxVersions: Int = 0,
    val casRequired: Boolean = false,
    val deleteVersionAfter: String = "0s",
)
