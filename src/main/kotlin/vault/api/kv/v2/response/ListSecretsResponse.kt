package dev.westelh.vault.api.kv.v2.response

import dev.westelh.vault.api.GetSecretResponse

typealias ListSecretsResponse = GetSecretResponse<ListSecretsResponseData>

data class ListSecretsResponseData(
    val keys: List<String>
)
