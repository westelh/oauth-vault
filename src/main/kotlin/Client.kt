package dev.westelh

import dev.westelh.vault.Config
import dev.westelh.vault.Vault
import dev.westelh.vault.api.kv.v2.request.PutSecretMetadataRequest
import dev.westelh.vault.api.kv.v2.request.PutSecretRequest
import dev.westelh.vault.kv
import io.ktor.server.config.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

class Client(vault: Vault, mount: String) {
    private fun uniquePath(userId: String): String = "applications/oauth/$userId"
    private val kv = vault.kv(mount)

    suspend fun readToken(boundUserId: String, version: Int? = null): Result<OAuthCodes> {
        return kv.readSecretVersion(uniquePath(boundUserId), version).mapCatching { response ->
            Json.decodeFromJsonElement(response.data.data)
        }
    }

    suspend fun writeToken(boundUserId: String, token: OAuthCodes): Result<Unit> {
        val payload = PutSecretRequest(Json.encodeToJsonElement(token))
        return kv.writeSecret(uniquePath(boundUserId), payload).mapCatching {
            // Result<PutSecretResponse>はユーザーには必要ないのでUnitに変換
        }
    }

    suspend fun readTokenMetadata(boundUserId: String): Result<UserProfile> {
        return kv.readSecretMetadata(uniquePath(boundUserId)).mapCatching { response ->
            Json.decodeFromJsonElement(response.data.customMetadata)
        }
    }

    suspend fun writeTokenMetadata(boundUserId: String, metadata: UserProfile): Result<Unit> {
        return kv.writeSecretMetadata(uniquePath(boundUserId), PutSecretMetadataRequest(metadata))
    }

    suspend fun deleteToken(boundUserId: String): Result<Unit> {
        return kv.deleteLatestVersionOfSecret(uniquePath(boundUserId))
    }
}

class VaultApplicationConfig(config: ApplicationConfig): Config {
    override val address: String = config.property("vault.addr").getString()
    override val token: String = config.propertyOrNull("vault.token")?.getString().orEmpty()
    val mount = config.property("vault.kv").getString()
}

fun createVaultClient(config: ApplicationConfig): Client {
    val vac = VaultApplicationConfig(config)
    val client = Vault(vac)
    return Client(client, vac.mount)
}