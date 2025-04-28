package dev.westelh

import io.ktor.server.auth.OAuthAccessTokenResponse

class Service(
    val id: IdentityService,
    val kv: KvService,
    val google: GoogleService
) {
    suspend fun getAndRefreshUserToken(userId: String): Result<Unit> {
        return kv.getUserOauthCodes(userId).mapCatching {
            val tok = it.refreshToken!!
            val new = google.refreshUserToken(tok).getOrThrow()
            kv.patchUserOauthCodes(userId, new).getOrThrow()
        }
    }

    suspend fun initUser(init: OAuthAccessTokenResponse.OAuth2): Result<Unit> = runCatching {
        val user = google.getUser(init.accessToken).getOrThrow()
        kv.writeUserProfile(user).getOrThrow()
        kv.writeUserOauthCodes(user.id, OAuthCodes(init)).getOrThrow()
    }
}
