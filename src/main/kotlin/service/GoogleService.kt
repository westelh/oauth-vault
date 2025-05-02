package dev.westelh.service

import dev.westelh.model.UserProfile
import google.api.GoogleRefreshTokenRequest
import google.api.GoogleRefreshTokenResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.auth.OAuthServerSettings

class GoogleService(val http: HttpClient, val config: Config) {
    interface Config {
        val clientId: String
        val clientSecret: String
    }

    suspend fun getUser(accessToken: String): Result<UserProfile> {
        val res = http.get("https://www.googleapis.com/userinfo/v2/me") {
            bearerAuth(accessToken)
        }
        return if (res.status.isSuccess()) {
            Result.success(res.body<UserProfile>())
        } else {
            Result.failure(Exception("Failed to get user profile"))
        }
    }

    suspend fun refreshUserToken(refreshToken: String): Result<GoogleRefreshTokenResponse> {
        val req = buildRefreshRequest(refreshToken)
        val res = http.post("https://oauth2.googleapis.com/token") {
            headers {
                contentType(ContentType.Application.Json)
            }
            setBody(req)
        }
        return if (res.status.isSuccess()) {
            Result.success(res.body<GoogleRefreshTokenResponse>())
        } else {
            Result.failure(Exception(res.status.description))
        }
    }

    private fun buildRefreshRequest(refreshToken: String): GoogleRefreshTokenRequest {
        return GoogleRefreshTokenRequest(config.clientId, config.clientSecret, "refresh_token", refreshToken)
    }

    fun oauth2Settings(scopes: List<String>): OAuthServerSettings.OAuth2ServerSettings =
        OAuthServerSettings.OAuth2ServerSettings(
            name = "google",
            authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
            accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
            requestMethod = HttpMethod.Post,
            clientId = config.clientId,
            clientSecret = config.clientSecret,
            defaultScopes = scopes,
            extraAuthParameters = listOf("access_type" to "offline")
        )
}
