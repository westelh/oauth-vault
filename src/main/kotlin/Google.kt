package dev.westelh

import google.api.GoogleRefreshTokenRequest
import google.api.GoogleRefreshTokenResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    @SerialName("given_name")
    val givenName: String,
    @SerialName("family_name")
    val familyName: String,
    val picture: String,
    val email: String,
    @SerialName("verified_email")
    val verifiedEmail: Boolean
)

fun buildGoogleClient(): HttpClient = HttpClient(Apache) {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun getUser(accessToken: String): UserProfile? {
    val client = buildGoogleClient()
    val res = client.get("https://www.googleapis.com/userinfo/v2/me") { bearerAuth(accessToken) }
    return if (res.status.isSuccess()) res.body<UserProfile>() else null
}

suspend fun refresh(req: GoogleRefreshTokenRequest): GoogleRefreshTokenResponse? {
    val client = buildGoogleClient()

    val res = client.post("https://oauth2.googleapis.com/token") {
        headers {
            contentType(ContentType.Application.Json)
        }
        setBody(req)
    }

    return if (res.status.isSuccess()) res.body() else null
}

suspend fun Application.refresh(refreshToken: String): GoogleRefreshTokenResponse? {
    return refresh(
        GoogleRefreshTokenRequest(
            clientId = environment.config.property("google.oauth.clientId").getString(),
            clientSecret = environment.config.property("google.oauth.clientSecret").getString(),
            "refresh_token",
            refreshToken
        )
    )
}

