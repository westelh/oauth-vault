package dev.westelh.service

import dev.westelh.model.OAuthCodes
import dev.westelh.model.UserProfile
import dev.westelh.vault.api.kv.v2.Kv
import dev.westelh.vault.api.kv.v2.request.PatchSecretRequest
import dev.westelh.vault.api.kv.v2.request.PutSecretMetadataRequest
import dev.westelh.vault.api.kv.v2.request.PutSecretRequest
import google.api.GoogleRefreshTokenResponse
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

class KvService(val kv: Kv) {
    private fun buildUserSecretPath(userId: String): String {
        return "applications/oauth/$userId"
    }

    private fun buildPatchRequest(from: GoogleRefreshTokenResponse): PatchSecretRequest {
        return PatchSecretRequest(
            data = JsonObject(
                mapOf(
                    "accessToken" to Json.encodeToJsonElement(from.accessToken),
                    "expiresIn" to Json.encodeToJsonElement(from.expiresIn),
                    "createdAt" to Json.encodeToJsonElement(Clock.System.now().epochSeconds),
                )
            )
        )
    }

    suspend fun getUserOauthCodes(userId: String): Result<OAuthCodes> {
        return kv.readSecretVersion(buildUserSecretPath(userId)).mapCatching { res ->
            Json.decodeFromJsonElement<OAuthCodes>(res.data.data)
        }
    }

    suspend fun writeUserOauthCodes(userId: String, oauthCodes: OAuthCodes): Result<Unit> {
        val payload = PutSecretRequest(Json.encodeToJsonElement(oauthCodes))
        return kv.writeSecret(buildUserSecretPath(userId), payload).map {
            // Result<PutSecretResponse>はユーザーには必要ないのでUnitに変換
        }
    }

    suspend fun patchUserOauthCodes(userId: String, from: GoogleRefreshTokenResponse): Result<Unit> {
        return kv.patchSecret(buildUserSecretPath(userId), buildPatchRequest(from)).map {
            // Result<PatchSecretResponse>はユーザーには必要ないのでUnitに変換
        }
    }

    suspend fun deleteUserOauthCodes(userId: String): Result<Unit> {
        return kv.deleteLatestVersionOfSecret(buildUserSecretPath(userId))
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return kv.readSecretMetadata(buildUserSecretPath(userId)).mapCatching { res ->
            Json.decodeFromJsonElement<UserProfile>(res.data.customMetadata)
        }
    }

    suspend fun writeUserProfile(profile: UserProfile): Result<Unit> {
        val payload = PutSecretMetadataRequest(Json.encodeToJsonElement(profile))
        return kv.writeSecretMetadata(buildUserSecretPath(profile.id), payload)
    }
}
