package dev.westelh.vault.api.kv.v2

import dev.westelh.vault.Vault
import dev.westelh.vault.api.kv.v2.request.*
import dev.westelh.vault.api.kv.v2.response.*
import io.ktor.client.request.*

class Kv(private val vault: Vault, private val mount: String) {
    class PathBuilder(private val mount: String) {
        fun buildConfigurationPath(): String = "$mount/config"
        fun buildSecretDataPath(path: String, version: Int?): String
            = "$mount/data/$path".appendVersionQueryIfNotNull(version)
        fun buildSecretSubkeysPath(path: String, version: Int?): String
            = "$mount/subkeys/$path".appendVersionQueryIfNotNull(version)
        fun buildSecretDeletePath(path: String): String = "$mount/delete/$path"
        fun buildSecretUndeletePath(path: String): String = "$mount/undelete/$path"
        fun buildSecretDestroyPath(path: String): String = "$mount/destroy/$path"
        fun buildSecretMetadataPath(path: String): String = "$mount/metadata/$path"
    }

    private val pathBuilder = PathBuilder(mount)

    // Configuration
    suspend fun writeConfig(payload: PutKvConfigRequest): Result<Unit>
        = vault.postOrVaultError(pathBuilder.buildConfigurationPath())

    suspend fun readConfig(): Result<GetKvConfigResponse>
        = vault.getOrVaultError(pathBuilder.buildConfigurationPath())

    // Read secret version
    suspend fun readSecretLatest(path: String): Result<GetSecretVersionResponse> =
        vault.getOrVaultError(pathBuilder.buildSecretDataPath(path, null))

    suspend fun readSecretVersion(path: String, version: Int? = null): Result<GetSecretVersionResponse> =
        vault.getOrVaultError(pathBuilder.buildSecretDataPath(path, version))

    // Create/Update secret
    suspend fun writeSecret(path: String, payload: PutSecretRequest): Result<PutSecretResponse> =
        vault.postOrVaultError(pathBuilder.buildSecretDataPath(path, null)) {
            setBody(payload)
        }

    // Patch secret
    suspend fun patchSecret(path: String, payload: PatchSecretRequest): Result<PatchSecretResponse> =
        vault.patchOrVaultError(pathBuilder.buildSecretDataPath(path, null)) {
            setBody(payload)
        }

    // Read secret subkeys
    suspend fun readSecretSubkeys(path: String, version: Int?): Result<GetSecretSubkeysResponse> =
        vault.getOrVaultError(pathBuilder.buildSecretSubkeysPath(path, version))

    // Delete
    suspend fun deleteLatestVersionOfSecret(path: String): Result<Unit> =
        vault.deleteOrVaultError(pathBuilder.buildSecretDataPath(path, null))

    suspend fun deleteSecretVersions(path: String, payload: SecretVersionSpec): Result<Unit> =
        vault.postOrVaultError(pathBuilder.buildSecretDeletePath(path)) {
            setBody(payload)
        }

    suspend fun undeleteSecretVersions(path: String, payload: SecretVersionSpec): Result<Unit> =
        vault.postOrVaultError(pathBuilder.buildSecretUndeletePath(path)) {
            setBody(payload)
        }

    // Destroy
    suspend fun destroySecretVersions(path: String, payload: SecretVersionSpec): Result<Unit> =
        vault.putOrVaultError<Unit>(pathBuilder.buildSecretDestroyPath(path)) {
            setBody(payload)
        }

    // List
    suspend fun listSecrets(path: String): Result<ListSecretsResponse>
        = vault.listOrVaultError(pathBuilder.buildSecretMetadataPath(path))

    // Metadata
    suspend fun readSecretMetadata(path: String): Result<GetSecretMetadataResponse> =
        vault.getOrVaultError(pathBuilder.buildSecretMetadataPath(path))

    suspend fun writeSecretMetadata(path: String, payload: PutSecretMetadataRequest): Result<Unit> =
        vault.postOrVaultError(pathBuilder.buildSecretMetadataPath(path)) {
            setBody(payload)
        }

    suspend fun patchSecretMetadata(path: String, payload: PatchSecretMetadataRequest): Result<Unit> =
        vault.patchOrVaultError(pathBuilder.buildSecretMetadataPath(path)) {
            setBody(payload)
        }

    // Delete metadata and all versions
    suspend fun deleteMetadataAndAllVersions(path: String): Result<Unit> =
        vault.deleteOrVaultError(pathBuilder.buildSecretMetadataPath(path))
}

private fun String.appendVersionQueryIfNotNull(version: Int?): String {
    return if (version != null) {
        "$this?version=$version"
    } else {
        this
    }
}