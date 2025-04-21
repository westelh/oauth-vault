package dev.westelh.vault.api.kv.v2

import dev.westelh.vault.Vault
import dev.westelh.vault.api.kv.v2.request.DeleteSecretVersionsRequest
import dev.westelh.vault.api.kv.v2.request.PutSecretMetadataRequest
import dev.westelh.vault.api.kv.v2.request.PutSecretRequest
import dev.westelh.vault.api.kv.v2.response.GetSecretMetadataResponse
import dev.westelh.vault.api.kv.v2.response.GetSecretVersionResponse
import dev.westelh.vault.api.kv.v2.response.PutSecretResponse
import io.ktor.client.request.*

class Kv(private val vault: Vault, private val mount: String) {
    // Configure the KV engine
    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#configure-the-kv-engine
    // suspend fun writeConfig(payload: KvV2ConfigureRequest): Result<KvV2ConfigureResponse>

    // Read KV engine configuration
    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-kv-engine-configuration
    // suspend fun readConfig()

    // Read secret version
    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-version
    suspend fun readSecretVersion(path: String, version: Int? = null): Result<GetSecretVersionResponse> =
        vault.getOrVaultError("$mount/data/$path".appendVersionQueryIfNotNull(version))

    // Create/Update secret
    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#create-update-secret
    suspend fun writeSecret(path: String, payload: PutSecretRequest): Result<PutSecretResponse> =
        vault.postOrVaultError("$mount/data/$path") {
            setBody(payload)
        }

    // Patch secret
    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#patch-secret
    // suspend fun patchSecret(path: String, payload: PutSecretRequest): Result<PutSecretResponse> =

    // Read secret subkeys
    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-subkeys
    // suspend fun readSecretSubkeys(path: String): Result<KvV2ReadSecretSubkeysResponse>

    // Delete latest version of secret
    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#delete-latest-version-of-secret
    suspend fun deleteLatestVersionOfSecret(path: String): Result<Unit> =
        vault.deleteOrVaultError("$mount/data/$path")

    // Delete secret versions
    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#delete-secret-versions
    suspend fun deleteSecretVersions(path: String, payload: DeleteSecretVersionsRequest): Result<Unit> =
        vault.postOrVaultError("$mount/delete/$path") {
            setBody(payload)
        }

    // Undelete secret versions
    // https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#undelete-secret-versions
    suspend fun undeleteSecretVersions(path: String, payload: DeleteSecretVersionsRequest): Result<Unit> =
        vault.postOrVaultError("$mount/undelete/$path") {
            setBody(payload)
        }

    // Destroy secret versions
    suspend fun destroySecretVersions(path: String, payload: DeleteSecretVersionsRequest): Result<Unit> =
        vault.postOrVaultError("$mount/destroy/$path") {
            setBody(payload)
        }

    // List secret versions
    // suspend fun listSecretVersions(path: String): Result<List<Int>>

    // Read secret metadata
    suspend fun readSecretMetadata(path: String): Result<GetSecretMetadataResponse> =
        vault.getOrVaultError("$mount/metadata/$path")

    // Create/Update metadata
    suspend fun writeSecretMetadata(path: String, payload: PutSecretMetadataRequest): Result<Unit> =
        vault.postOrVaultError("$mount/metadata/$path") {
            setBody(payload)
        }

    // Patch metadata
    // suspend fun pathSecretMetadata(path: String, payload: PutSecretMetadataRequest): Result<Unit> =

    // Delete metadata and all versions
    suspend fun deleteMetadataAndAllVersions(path: String): Result<Unit> =
        vault.deleteOrVaultError("$mount/metadata/$path")
}

private fun String.appendVersionQueryIfNotNull(version: Int?): String {
    return if (version != null) {
        "$this?version=$version"
    } else {
        this
    }
}