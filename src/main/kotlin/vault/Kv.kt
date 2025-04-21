package dev.westelh.vault

import dev.westelh.vault.api.kv.*
import io.ktor.client.request.*

private fun MountedPath.secret(path: String): String = complete("data/$path")
private fun MountedPath.metadata(path: String): String = complete("metadata/$path")

suspend fun Vault.readKvV2Secret(mount: String, path: String): Result<KvV2GetSecretResponse> = this.getOrVaultError(baseUrl.mount(mount).secret(path))

suspend fun Vault.writeKvV2Secret(mount: String, path: String, payload: KvV2WriteSecretRequest): Result<KvV2WriteSecretResponse> = this.postOrVaultError(baseUrl.mount(mount).secret(path)) {
    setBody(payload)
}

suspend fun Vault.deleteKvV2Secret(mount: String, path: String): Result<Unit> = this.deleteOrVaultError(baseUrl.mount(mount).secret(path))

suspend fun Vault.readKvV2Metadata(mount: String, path: String): Result<KvV2GetMetadataResponse> = this.getOrVaultError(baseUrl.mount(mount).metadata(path))

suspend fun Vault.writeKvV2Metadata(mount: String, path: String, payload: KvV2WriteMetadataRequest): Result<Unit> = this.postOrVaultError(baseUrl.mount(mount).metadata(path)) {
    setBody(payload)
}
