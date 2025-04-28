package dev.westelh.vault.api.kv.v2.response

import dev.westelh.vault.api.GetSecretResponse
import dev.westelh.vault.api.kv.v2.MetadataSnapshot

typealias PutSecretResponse = GetSecretResponse<MetadataSnapshot>
typealias PatchSecretResponse = GetSecretResponse<MetadataSnapshot>
