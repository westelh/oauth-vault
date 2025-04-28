package dev.westelh

import google.api.GoogleRefreshTokenRequest
import google.api.GoogleRefreshTokenResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.ApplicationConfig

interface GoogleService {
    val clientEngine: HttpClientEngine
    val clientId: String
    val clientSecret: String

    private fun buildClient(): HttpClient = HttpClient(clientEngine) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getUser(accessToken: String): Result<UserProfile> {
        val client = buildClient()
        val res = client.get("https://www.googleapis.com/userinfo/v2/me") {
            bearerAuth(accessToken)
        }
        return if (res.status.isSuccess()) {
            Result.success(res.body<UserProfile>())
        } else {
            Result.failure(Exception("Failed to get user profile"))
        }
    }

    suspend fun refreshUserToken(refreshToken: String): Result<GoogleRefreshTokenResponse> {
        val client = buildClient()
        val req = buildRefreshRequest(refreshToken)
        val res = client.post("https://oauth2.googleapis.com/token") {
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
        return GoogleRefreshTokenRequest(clientId, clientSecret, "refresh_token", refreshToken)
    }
}

class ApplicationGoogleService(config: ApplicationConfig, engine: HttpClientEngine): GoogleService {
    override val clientEngine: HttpClientEngine = engine
    override val clientId: String = config.property("google.oauth.clientId").getString()
    override val clientSecret: String = config.property("google.oauth.clientSecret").getString()
}