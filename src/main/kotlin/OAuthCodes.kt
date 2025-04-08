package dev.westelh

import io.ktor.server.auth.*
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

@Serializable
data class OAuthCodes(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshToken: String?,
    val extraParameters: Map<String, List<String>>,
    val state: String?,
    val createdAt: Instant = Clock.System.now()
) {
    constructor(library: OAuthAccessTokenResponse.OAuth2): this(
        library.accessToken,
        library.tokenType,
        library.expiresIn,
        library.refreshToken,
        library.extraParameters.toMap(),
        library.state
    )
}

fun OAuthCodes.expiresAt(): Instant = createdAt.plus(expiresIn, DateTimeUnit.SECOND)