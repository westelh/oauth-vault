package dev.westelh

import dev.westelh.vault.*
import dev.westelh.vault.api.kv.KvV2WriteMetadataRequest
import dev.westelh.vault.api.kv.KvV2WriteSecretRequest
import dev.westelh.vault.api.kv.KvV2WriteSecretResponse
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

class Client(private val vault: Vault, private val mount: String) {
    private fun uniquePath(userId: String): String = "applications/oauth/$userId"

    suspend fun readToken(boundUserId: String): Result<OAuthCodes> {
        return vault.readKvV2Secret(mount, uniquePath(boundUserId)).mapCatching { response ->
            Json.decodeFromJsonElement(response.data.data)
        }
    }

    suspend fun readToken(boundUserId: String, version: Int): Result<OAuthCodes> {
        return vault.readKvV2Secret(mount, uniquePath(boundUserId)).mapCatching { response ->
            Json.decodeFromJsonElement(response.data.data)
        }
    }

    suspend fun writeToken(boundUserId: String, token: OAuthCodes): Result<KvV2WriteSecretResponse> {
        val payload = KvV2WriteSecretRequest(Json.encodeToJsonElement(token))
        return vault.writeKvV2Secret(mount, uniquePath(boundUserId), payload)
    }

    suspend fun readTokenMetadata(boundUserId: String): Result<UserProfile> {
        return vault.readKvV2Metadata(mount, uniquePath(boundUserId)).mapCatching { response ->
            Json.decodeFromJsonElement(response.data.customMetadata)
        }
    }

    suspend fun writeTokenMetadata(boundUserId: String, metadata: UserProfile): Result<Unit> {
        return vault.writeKvV2Metadata(mount, uniquePath(boundUserId), KvV2WriteMetadataRequest(metadata))
    }

    suspend fun deleteToken(boundUserId: String): Result<Unit> {
        return vault.deleteKvV2Secret(mount, uniquePath(boundUserId))
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