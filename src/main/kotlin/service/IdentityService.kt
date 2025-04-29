package dev.westelh.service

import dev.westelh.model.GoogleIdentityData
import dev.westelh.model.OpenIdProviderMetadata
import dev.westelh.vault.api.identity.Identity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class IdentityService(private val identity: Identity) {

    suspend fun getOidcClientId(name: String): Result<String> {
        return identity.readOidcClient(name).map { it.data.clientId }
    }

    suspend fun getOidcClientSecret(name: String): Result<String> {
        return identity.readOidcClient(name).map { it.data.clientSecret }
    }

    suspend fun getOidcConfiguration(providerName: String): Result<OpenIdProviderMetadata> {
        return identity.readOidcProviderConfiguration(providerName)
    }

    fun buildProviderLookup(providerName: String, clientName: String, scopes: List<String>): OAuthServerSettings.OAuth2ServerSettings {
        val config = runBlocking { getOidcConfiguration(providerName) }.getOrThrow()
        return OAuthServerSettings.OAuth2ServerSettings(
            name = providerName,
            requestMethod = HttpMethod.Companion.Post,
            authorizeUrl = config.authorizationEndpoint,
            accessTokenUrl = config.tokenEndpoint!!,
            clientId = runBlocking { getOidcClientId(clientName) }.getOrThrow(),
            clientSecret = runBlocking { getOidcClientSecret(clientName) }.getOrThrow(),
            defaultScopes = scopes,
            onStateCreated = ::onOidcStateCreated
        )
    }

    fun onOidcStateCreated(call: ApplicationCall, state: String) {
        call.request.queryParameters["error"]?.let {
            val desc = call.request.queryParameters["error_description"].orEmpty()
            call.application.log.error("Error during oauth: $it - $desc")
        }
    }

    suspend fun getGoogleIdFromOidcProvider(providerName: String, accessToken: String): Result<GoogleIdentityData> = runCatching {
        val json = identity.readOidcUserInfo(providerName, accessToken).getOrThrow()
        val go = json.jsonObject["google"]!!
        return@runCatching Json.decodeFromJsonElement<GoogleIdentityData>(go)
    }
}
