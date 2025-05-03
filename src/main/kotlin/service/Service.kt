package dev.westelh.service

suspend fun refreshOauthCode(userId: String, kv: KvService, google: GoogleService): Result<Unit> = runCatching {
    val codes = kv.getUserOauthCodes(userId).getOrElse { e -> throw RuntimeException("User token is missing", e) }
    val refresh = codes.refreshToken ?: throw RuntimeException("Refresh token is missing")
    val new = google.refreshUserToken(refresh).getOrElse { e -> throw RuntimeException("Refresh token is invalid", e) }
    kv.patchUserOauthCodes(userId, new).getOrElse { e -> throw RuntimeException("Failed to patch user codes") }
}